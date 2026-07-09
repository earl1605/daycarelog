package edu.cit.mahumot.daycarelog.common.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Confirms a domain can plausibly receive mail (has an MX record, or an A record as
// the RFC 5321 §5.1 fallback) before accepting a registration. Uses the JDK's own
// JNDI DNS provider (com.sun.jndi.dns, bundled with every JDK - no new dependency)
// rather than pulling in dnsjava.
@Component
public class MxRecordService {

    private static final Logger log = LoggerFactory.getLogger(MxRecordService.class);

    private static final long CACHE_TTL_MILLIS = 60L * 60 * 1000; // 1 hour
    private static final long DNS_TIMEOUT_MILLIS = 3000; // 3 seconds

    @FunctionalInterface
    interface DnsLookup {
        boolean hasMailRecords(String domain) throws NamingException;
    }

    private final boolean mxCheckEnabled;
    private final DnsLookup dnsLookup;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    public MxRecordService(@Value("${app.email.mx-check-enabled:true}") boolean mxCheckEnabled) {
        this(mxCheckEnabled, MxRecordService::realDnsHasMailRecords);
    }

    // Package-visible so tests can inject a fake DNS lookup - avoids real network
    // calls and makes timeout/failure fail-open behavior deterministic to test.
    MxRecordService(boolean mxCheckEnabled, DnsLookup dnsLookup) {
        this.mxCheckEnabled = mxCheckEnabled;
        this.dnsLookup = dnsLookup;
    }

    public void validate(String domain) {
        if (!mxCheckEnabled) return;
        if (!hasValidMx(domain)) {
            throw new EmailValidationException(
                    "EMAIL_DOMAIN_INVALID",
                    "This email domain cannot receive mail. Please check for typos or use a different email.");
        }
    }

    boolean hasValidMx(String domain) {
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(domain);
        if (cached != null && cached.expiresAt > now) {
            return cached.valid;
        }

        boolean valid;
        try {
            valid = dnsLookup.hasMailRecords(domain);
        } catch (Exception e) {
            // Transient DNS failures (timeout, unreachable resolver) must not block a
            // real user from registering - fail open, but make it visible in logs.
            log.warn("DNS lookup failed for domain '{}' - allowing registration to proceed (fail-open)", domain, e);
            valid = true;
        }

        cache.put(domain, new CacheEntry(valid, now + CACHE_TTL_MILLIS));
        return valid;
    }

    private static boolean realDnsHasMailRecords(String domain) throws NamingException {
        return lookup(domain, "MX") || lookup(domain, "A");
    }

    // Returns a clean true/false for "does this record exist" - NameNotFoundException
    // (NXDOMAIN, or domain exists with no records of this type) is treated as a
    // confident "no", not a failure. Anything else (timeout, SERVFAIL, unreachable
    // server) propagates so the caller can fail-open instead of rejecting outright.
    private static boolean lookup(String domain, String recordType) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(DNS_TIMEOUT_MILLIS));
        env.put("com.sun.jndi.dns.timeout.retries", "1");

        DirContext ctx = new InitialDirContext(env);
        try {
            Attributes attrs = ctx.getAttributes(domain, new String[]{recordType});
            Attribute record = attrs.get(recordType);
            return record != null && record.size() > 0;
        } catch (NameNotFoundException e) {
            return false;
        } finally {
            ctx.close();
        }
    }

    private record CacheEntry(boolean valid, long expiresAt) {}
}
