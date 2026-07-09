package edu.cit.mahumot.daycarelog.common.email;

import org.springframework.stereotype.Component;

// Single entry point for "is this email worth sending a verification message to" -
// used by both public STAFF self-registration and Admin/Staff-created PARENT
// accounts, so the two flows can never drift apart. Runs format -> disposable/
// reserved-domain blocklist -> MX record check, in that order, returning the first
// failure only.
@Component
public class EmailRegistrationValidator {

    private final EmailFormatValidator formatValidator;
    private final DisposableEmailService disposableEmailService;
    private final MxRecordService mxRecordService;

    public EmailRegistrationValidator(EmailFormatValidator formatValidator,
                                       DisposableEmailService disposableEmailService,
                                       MxRecordService mxRecordService) {
        this.formatValidator = formatValidator;
        this.disposableEmailService = disposableEmailService;
        this.mxRecordService = mxRecordService;
    }

    /** @return the normalized (trimmed, lowercased) email, once all three layers pass. */
    public String validate(String rawEmail) {
        String email = formatValidator.normalizeAndValidate(rawEmail);
        String domain = email.substring(email.indexOf('@') + 1);
        disposableEmailService.validate(domain);
        mxRecordService.validate(domain);
        return email;
    }
}
