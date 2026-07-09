package edu.cit.mahumot.daycarelog.features.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.mahumot.daycarelog.common.email.EmailValidationException;
import edu.cit.mahumot.daycarelog.common.email.MxRecordService;
import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Covers the registration-time email guard layers end to end (format -> disposable ->
// MX), on top of the unit tests for each individual layer. The MX layer is faked here
// via @MockitoBean rather than hitting real DNS - the test/application.properties
// default (app.email.mx-check-enabled=false) would otherwise skip it entirely.
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationEmailGuardIntegrationTest {

    private static final String PASSWORD = "TestPass123";
    private static final String GMAIL = "guarded-gmail-test@gmail.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @MockitoSpyBean private EmailService emailService;
    @MockitoBean private MxRecordService mxRecordService;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(GMAIL).ifPresent(userRepository::delete);
        doCallRealMethod().when(emailService)
                .sendVerificationEmail(anyString(), any(), anyString(), anyString());
        doNothing().when(mxRecordService).validate(anyString());
    }

    private Map<String, Object> registerBody(String email) {
        return Map.of("email", email, "password", PASSWORD, "firstName", "Guard", "lastName", "Test");
    }

    @Test
    void registeringWithADisposableEmailDomainIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody("someone@mailinator.com"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DISPOSABLE_EMAIL"));

        verifyNoInteractions(emailService);
    }

    @Test
    void registeringWithADomainThatCannotReceiveMailIsRejected() throws Exception {
        String domain = "nonexistent-domain-abc123.xyz";
        doThrow(new EmailValidationException("EMAIL_DOMAIN_INVALID", "This email domain cannot receive mail."))
                .when(mxRecordService).validate(domain);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody("someone@" + domain))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("EMAIL_DOMAIN_INVALID"));

        verifyNoInteractions(emailService);
    }

    @Test
    void registeringWithAValidLookingGmailAddressCreatesAnUnverifiedAccountAndSendsVerificationEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerBody(GMAIL))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist());

        var user = userRepository.findByEmail(GMAIL).orElseThrow();
        assertThat(user.getEmailVerified()).isFalse();
        verify(emailService).sendVerificationEmail(eq(GMAIL), any(), anyString(), anyString());
    }

    @Test
    void registeringWithAnAlreadyRegisteredEmailReturnsTheSameGenericResponseAsSuccessWithNoNewVerificationEmail() throws Exception {
        Map<String, Object> body = registerBody(GMAIL);

        String firstResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        // identical response shape for "already registered" as for "brand new" - no enumeration
        assertThat(secondResponse).isEqualTo(firstResponse);
        // exactly one verification email for the real (first) registration only
        verify(emailService, times(1)).sendVerificationEmail(eq(GMAIL), any(), anyString(), anyString());
    }
}
