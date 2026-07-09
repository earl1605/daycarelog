package edu.cit.mahumot.daycarelog.features.verification;

// Carries a machine-readable error code alongside the human-readable message, so
// clients (web + Android) can branch on `code` (TOKEN_EXPIRED, TOKEN_INVALID,
// TOO_MANY_ATTEMPTS, RATE_LIMITED) without parsing the message text.
public class VerificationException extends RuntimeException {
    private final String code;

    public VerificationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
