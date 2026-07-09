package edu.cit.mahumot.daycarelog.features.verification;

public class VerificationException extends RuntimeException {
    private final String code;

    public VerificationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
