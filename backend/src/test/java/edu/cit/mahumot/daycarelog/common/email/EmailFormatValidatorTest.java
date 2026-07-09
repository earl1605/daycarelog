package edu.cit.mahumot.daycarelog.common.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailFormatValidatorTest {

    private final EmailFormatValidator validator = new EmailFormatValidator();

    @Test
    void validGmailAddressPasses() {
        assertThat(validator.normalizeAndValidate("someone@gmail.com")).isEqualTo("someone@gmail.com");
    }

    @Test
    void normalizesToLowercaseAndTrimsWhitespaceAroundTheAddress() {
        assertThat(validator.normalizeAndValidate("  Someone@Gmail.COM  ")).isEqualTo("someone@gmail.com");
    }

    @Test
    void blankOrNullIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(""))
                .isInstanceOf(EmailValidationException.class)
                .satisfies(e -> assertThat(((EmailValidationException) e).getCode()).isEqualTo("EMAIL_INVALID_FORMAT"));
        assertThatThrownBy(() -> validator.normalizeAndValidate(null))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void embeddedSpaceIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("some one@gmail.com"))
                .isInstanceOf(EmailValidationException.class)
                .satisfies(e -> assertThat(((EmailValidationException) e).getCode()).isEqualTo("EMAIL_INVALID_FORMAT"));
    }

    @Test
    void missingAtSignIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone.gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void multipleAtSignsAreRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("some@one@gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void leadingDotInLocalPartIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(".someone@gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void trailingDotInLocalPartIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone.@gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void consecutiveDotsInLocalPartAreRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("some..one@gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void consecutiveDotsInDomainAreRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone@gmail..com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void domainWithNoTldIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone@gmail"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void tldThatIsNotAlphabeticIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone@gmail.123"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void domainLabelStartingOrEndingWithHyphenIsRejected() {
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone@-gmail.com"))
                .isInstanceOf(EmailValidationException.class);
        assertThatThrownBy(() -> validator.normalizeAndValidate("someone@gmail-.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void addressLongerThan254CharactersIsRejected() {
        String longLocal = "a".repeat(250);
        assertThatThrownBy(() -> validator.normalizeAndValidate(longLocal + "@gmail.com"))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void subdomainsAndPlusAddressingAreAccepted() {
        assertThat(validator.normalizeAndValidate("user+tag@mail.example-provider.com"))
                .isEqualTo("user+tag@mail.example-provider.com");
    }
}
