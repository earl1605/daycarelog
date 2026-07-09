package edu.cit.mahumot.daycarelog.common.email;

// Carries a machine-readable error code (EMAIL_INVALID_FORMAT, DISPOSABLE_EMAIL,
// EMAIL_DOMAIN_INVALID) alongside the human-readable message, same pattern as
// features.verification.VerificationException.
public class EmailValidationException extends RuntimeException {
    private final String code;

    public EmailValidationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
