package edu.cit.mahumot.daycarelog.common.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisposableEmailServiceTest {

    private DisposableEmailService service;

    @BeforeEach
    void setUp() {
        service = new DisposableEmailService();
        service.loadBlocklist(); // @PostConstruct isn't invoked without a Spring context
    }

    @Test
    void blocklistFileIsLoadedWithAtLeastFiftyDomains() {
        assertThat(service.blocklistSize()).isGreaterThanOrEqualTo(50);
    }

    @Test
    void knownDisposableDomainsAreBlocked() {
        assertThat(service.isBlocked("mailinator.com")).isTrue();
        assertThat(service.isBlocked("guerrillamail.com")).isTrue();
        assertThat(service.isBlocked("10minutemail.com")).isTrue();
        assertThat(service.isBlocked("yopmail.com")).isTrue();
        assertThat(service.isBlocked("sharklasers.com")).isTrue();
        assertThat(service.isBlocked("maildrop.cc")).isTrue();
    }

    @Test
    void realProvidersThatSoundGenericAreNotBlocked() {
        assertThat(service.isBlocked("gmail.com")).isFalse();
        assertThat(service.isBlocked("yahoo.com")).isFalse();
        assertThat(service.isBlocked("outlook.com")).isFalse();
        assertThat(service.isBlocked("email.com")).isFalse();
        assertThat(service.isBlocked("mail.com")).isFalse();
    }

    @Test
    void rfc2606ReservedDomainsAreBlocked() {
        assertThat(service.isBlocked("example.com")).isTrue();
        assertThat(service.isBlocked("example.net")).isTrue();
        assertThat(service.isBlocked("example.org")).isTrue();
        assertThat(service.isBlocked("example.edu")).isTrue();
    }

    @Test
    void rfc2606ReservedTldsAreBlockedRegardlessOfDomainName() {
        assertThat(service.isBlocked("anything.test")).isTrue();
        assertThat(service.isBlocked("anything.invalid")).isTrue();
        assertThat(service.isBlocked("anything.localhost")).isTrue();
        assertThat(service.isBlocked("something.example")).isTrue();
    }

    @Test
    void unknownDomainIsNotBlocked() {
        assertThat(service.isBlocked("my-real-daycare-company.com")).isFalse();
    }

    @Test
    void nullOrEmptyDomainIsNotBlocked() {
        assertThat(service.isBlocked(null)).isFalse();
        assertThat(service.isBlocked("")).isFalse();
    }

    @Test
    void validateThrowsDisposableEmailForABlockedDomain() {
        assertThatThrownBy(() -> service.validate("mailinator.com"))
                .isInstanceOf(EmailValidationException.class)
                .satisfies(e -> assertThat(((EmailValidationException) e).getCode()).isEqualTo("DISPOSABLE_EMAIL"));
    }

    @Test
    void validateDoesNotThrowForARealDomain() {
        service.validate("gmail.com"); // must not throw
    }
}
