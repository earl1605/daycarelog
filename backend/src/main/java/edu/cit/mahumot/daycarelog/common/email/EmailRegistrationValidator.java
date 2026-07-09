package edu.cit.mahumot.daycarelog.common.email;

import org.springframework.stereotype.Component;

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

    public String validate(String rawEmail) {
        String email = formatValidator.normalizeAndValidate(rawEmail);
        String domain = email.substring(email.indexOf('@') + 1);
        disposableEmailService.validate(domain);
        mxRecordService.validate(domain);
        return email;
    }
}
