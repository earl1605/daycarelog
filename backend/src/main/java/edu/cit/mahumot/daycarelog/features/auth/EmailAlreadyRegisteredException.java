package edu.cit.mahumot.daycarelog.features.auth;

// Thrown when registration targets an email that already has an account -
// mirrors the code+message shape of EmailValidationException/VerificationException
// so AuthController can handle it the same way.
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("This email is already registered. Try logging in instead.");
    }

    public String getCode() { return "EMAIL_ALREADY_REGISTERED"; }
}
