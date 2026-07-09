package edu.cit.mahumot.daycarelog.common.email;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

// Stricter than a bare @Email annotation: rejects shapes that pass most basic email
// regexes but are still obviously wrong (double dots, no TLD, embedded whitespace).
@Component
public class EmailFormatValidator {

    private static final int MAX_LENGTH = 254; // RFC 5321 §4.5.3.1.3

    // Practical (not full RFC 5322) local-part/domain-label character sets.
    private static final Pattern LOCAL_PART = Pattern.compile("^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");
    private static final Pattern DOMAIN_LABEL = Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?$");
    private static final Pattern TLD = Pattern.compile("^[A-Za-z]{2,}$");

    /** Trims and lowercases, then validates. Returns the normalized email on success. */
    public String normalizeAndValidate(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw invalid("Email address is required.");
        }
        String email = rawEmail.trim().toLowerCase();

        if (email.length() > MAX_LENGTH) {
            throw invalid("Email address is too long.");
        }
        if (containsWhitespace(email)) {
            throw invalid("Email address cannot contain spaces.");
        }

        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            throw invalid("Please enter a valid email address.");
        }

        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        validateLocalPart(local);
        validateDomainPart(domain);

        return email;
    }

    private void validateLocalPart(String local) {
        if (local.startsWith(".") || local.endsWith(".")) {
            throw invalid("Email address cannot start or end with a dot.");
        }
        if (local.contains("..")) {
            throw invalid("Email address cannot contain consecutive dots.");
        }
        if (!LOCAL_PART.matcher(local).matches()) {
            throw invalid("Please enter a valid email address.");
        }
    }

    private void validateDomainPart(String domain) {
        if (domain.startsWith(".") || domain.endsWith(".") || domain.startsWith("-") || domain.endsWith("-")) {
            throw invalid("Please enter a valid email address.");
        }
        if (domain.contains("..")) {
            throw invalid("Email address cannot contain consecutive dots.");
        }
        String[] labels = domain.split("\\.", -1);
        if (labels.length < 2) {
            throw invalid("Email address is missing a domain extension (e.g. .com).");
        }
        for (String label : labels) {
            if (label.isEmpty() || !DOMAIN_LABEL.matcher(label).matches()) {
                throw invalid("Please enter a valid email address.");
            }
        }
        String tld = labels[labels.length - 1];
        if (!TLD.matcher(tld).matches()) {
            throw invalid("Email address is missing a valid domain extension (e.g. .com).");
        }
    }

    private boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return true;
        }
        return false;
    }

    private EmailValidationException invalid(String message) {
        return new EmailValidationException("EMAIL_INVALID_FORMAT", message);
    }
}
