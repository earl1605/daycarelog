package edu.cit.mahumot.daycarelog.common.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class EmailRegistrationValidatorTest {

    @Mock private EmailFormatValidator formatValidator;
    @Mock private DisposableEmailService disposableEmailService;
    @Mock private MxRecordService mxRecordService;

    private EmailRegistrationValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new EmailRegistrationValidator(formatValidator, disposableEmailService, mxRecordService);
    }

    @Test
    void formatFailureShortCircuitsBeforeDisposableOrMxChecksRun() {
        var formatError = new EmailValidationException("EMAIL_INVALID_FORMAT", "bad format");
        org.mockito.Mockito.when(formatValidator.normalizeAndValidate("bad")).thenThrow(formatError);

        assertThatThrownBy(() -> validator.validate("bad")).isSameAs(formatError);

        verifyNoInteractions(disposableEmailService, mxRecordService);
    }

    @Test
    void disposableFailureShortCircuitsBeforeMxCheckRuns() {
        org.mockito.Mockito.when(formatValidator.normalizeAndValidate("user@mailinator.com"))
                .thenReturn("user@mailinator.com");
        var disposableError = new EmailValidationException("DISPOSABLE_EMAIL", "disposable");
        org.mockito.Mockito.doThrow(disposableError).when(disposableEmailService).validate("mailinator.com");

        assertThatThrownBy(() -> validator.validate("user@mailinator.com")).isSameAs(disposableError);

        verifyNoInteractions(mxRecordService);
    }

    @Test
    void allThreeLayersRunInOrderAndTheNormalizedEmailIsReturnedOnSuccess() {
        org.mockito.Mockito.when(formatValidator.normalizeAndValidate("USER@Gmail.com"))
                .thenReturn("user@gmail.com");

        String result = validator.validate("USER@Gmail.com");

        assertThat(result).isEqualTo("user@gmail.com");
        verify(disposableEmailService).validate("gmail.com");
        verify(mxRecordService).validate("gmail.com");
    }
}
