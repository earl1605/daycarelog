package edu.cit.mahumot.daycarelog.common.email;

import org.junit.jupiter.api.Test;

import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MxRecordServiceTest {

    @Test
    void domainWithMailRecordsIsValid() {
        MxRecordService service = new MxRecordService(true, domain -> true);
        assertThat(service.hasValidMx(domain())).isTrue();
    }

    @Test
    void domainWithNoMailRecordsIsInvalid() {
        MxRecordService service = new MxRecordService(true, domain -> false);
        assertThat(service.hasValidMx(domain())).isFalse();
    }

    @Test
    void resultIsCachedSoTheLookupIsOnlyPerformedOnce() {
        AtomicInteger calls = new AtomicInteger();
        MxRecordService service = new MxRecordService(true, domain -> {
            calls.incrementAndGet();
            return true;
        });

        service.hasValidMx(domain());
        service.hasValidMx(domain());
        service.hasValidMx(domain());

        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void differentDomainsAreCachedIndependently() {
        MxRecordService service = new MxRecordService(true, domain -> domain.equals("good.com"));

        assertThat(service.hasValidMx("good.com")).isTrue();
        assertThat(service.hasValidMx("bad.com")).isFalse();
        // re-check to confirm both cached results stuck rather than one clobbering the other
        assertThat(service.hasValidMx("good.com")).isTrue();
        assertThat(service.hasValidMx("bad.com")).isFalse();
    }

    @Test
    void dnsExceptionFailsOpenInsteadOfRejectingTheDomain() {
        MxRecordService service = new MxRecordService(true, domain -> {
            throw new RuntimeException("simulated DNS timeout");
        });

        assertThat(service.hasValidMx(domain())).isTrue();
    }

    @Test
    void namingExceptionFailsOpenInsteadOfRejectingTheDomain() {
        MxRecordService service = new MxRecordService(true, domain -> {
            throw new NamingException("simulated resolver failure");
        });

        assertThat(service.hasValidMx(domain())).isTrue();
    }

    @Test
    void validateThrowsEmailDomainInvalidWhenMxCheckIsEnabledAndDomainHasNoRecords() {
        MxRecordService service = new MxRecordService(true, domain -> false);

        try {
            service.validate(domain());
            org.junit.jupiter.api.Assertions.fail("expected EmailValidationException");
        } catch (EmailValidationException e) {
            assertThat(e.getCode()).isEqualTo("EMAIL_DOMAIN_INVALID");
        }
    }

    @Test
    void validateDoesNotThrowWhenMxCheckIsEnabledAndDomainHasRecords() {
        MxRecordService service = new MxRecordService(true, domain -> true);
        service.validate(domain()); // must not throw
    }

    @Test
    void validateSkipsTheLookupEntirelyWhenTheToggleIsDisabled() {
        AtomicInteger calls = new AtomicInteger();
        MxRecordService service = new MxRecordService(false, domain -> {
            calls.incrementAndGet();
            return false; // would fail if the lookup were actually performed
        });

        service.validate(domain()); // must not throw even though the fake lookup returns false
        assertThat(calls.get()).isEqualTo(0);
    }

    private static String domain() {
        return "example-domain-under-test.com";
    }
}
