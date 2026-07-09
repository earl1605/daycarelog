package edu.cit.mahumot.daycarelog.common.email;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DisposableEmailService {

    private static final Logger log = LoggerFactory.getLogger(DisposableEmailService.class);

    // RFC 2606 reserves these exact domains, and these TLDs entirely, for
    // documentation/testing - they can never receive real mail. Deliberately NOT
    // included: domains that merely "sound fake" but are real providers (e.g.
    // email.com, mail.com) - only verifiably reserved/disposable domains are blocked.
    private static final Set<String> RESERVED_DOMAINS = Set.of(
            "example.com", "example.net", "example.org", "example.edu"
    );
    private static final Set<String> RESERVED_TLDS = Set.of(
            "test", "example", "invalid", "localhost"
    );

    private final Set<String> disposableDomains = ConcurrentHashMap.newKeySet();

    @PostConstruct
    void loadBlocklist() {
        try (var input = new ClassPathResource("disposable-domains.txt").getInputStream();
             var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String domain = line.trim().toLowerCase();
                if (domain.isEmpty() || domain.startsWith("#")) continue;
                disposableDomains.add(domain);
            }
            log.info("Loaded {} disposable email domains", disposableDomains.size());
        } catch (IOException e) {
            log.error("Failed to load disposable-domains.txt - disposable email blocking is disabled", e);
        }
    }

    /** @param domain already-lowercased domain part of an email (no leading '@'). */
    public boolean isBlocked(String domain) {
        if (domain == null || domain.isEmpty()) return false;
        if (RESERVED_DOMAINS.contains(domain)) return true;
        String tld = domain.substring(domain.lastIndexOf('.') + 1);
        if (RESERVED_TLDS.contains(tld)) return true;
        return disposableDomains.contains(domain);
    }

    public void validate(String domain) {
        if (isBlocked(domain)) {
            throw new EmailValidationException(
                    "DISPOSABLE_EMAIL",
                    "Temporary or disposable email addresses are not allowed. Please use a real email account.");
        }
    }

    /** Test/ops visibility only - not used by production validation logic. */
    int blocklistSize() {
        return disposableDomains.size();
    }
}
