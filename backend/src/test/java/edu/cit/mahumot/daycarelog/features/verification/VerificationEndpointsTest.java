package edu.cit.mahumot.daycarelog.features.verification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.mahumot.daycarelog.common.mail.EmailService;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationEndpointsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @MockitoSpyBean private EmailService emailService;

    private static final String EMAIL = "verify-test@test.daycarelog";
    private static final String PASSWORD = "TestPass123";

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(EMAIL).ifPresent(userRepository::delete);
        doCallRealMethod().when(emailService)
                .sendVerificationEmail(anyString(), any(), anyString(), anyString());
    }

    private String[] registerAndCaptureRawTokenAndCode() throws Exception {
        Map<String, Object> body = Map.of(
                "email", EMAIL, "password", PASSWORD,
                "firstName", "Verify", "lastName", "Test");
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(EMAIL), any(), tokenCaptor.capture(), codeCaptor.capture());
        return new String[]{tokenCaptor.getValue(), codeCaptor.getValue()};
    }

    @Test
    void registeredAccountStartsUnverifiedAndTokenIsBlockedFromOtherEndpoints() throws Exception {
        Map<String, Object> body = Map.of(
                "email", EMAIL, "password", PASSWORD,
                "firstName", "Verify", "lastName", "Test");
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user").doesNotExist());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);
        assertThat(json.get("user").get("emailVerified").asBoolean()).isFalse();
        String token = json.get("token").asText();

        mockMvc.perform(get("/api/children").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void verifyByLinkTokenSucceedsAndUnblocksTheAccount() throws Exception {
        String[] tokenAndCode = registerAndCaptureRawTokenAndCode();
        String rawToken = tokenAndCode[0];

        String response = mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andReturn().getResponse().getContentAsString();

        String freshToken = objectMapper.readTree(response).get("token").asText();
        mockMvc.perform(get("/api/children").header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(EMAIL).orElseThrow().getEmailVerified()).isTrue();
    }

    @Test
    void reusingAnAlreadyConsumedLinkTokenFails() throws Exception {
        String[] tokenAndCode = registerAndCaptureRawTokenAndCode();
        String rawToken = tokenAndCode[0];

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", rawToken))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    void verifyingWithAGarbageTokenFails() throws Exception {
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("token", "not-a-real-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
    }

    @Test
    void verifyByCodeSucceeds() throws Exception {
        String[] tokenAndCode = registerAndCaptureRawTokenAndCode();
        String rawCode = tokenAndCode[1];

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "code", rawCode))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    void wrongCodeFailsAndFifthWrongAttemptInvalidatesTheCode() throws Exception {
        registerAndCaptureRawTokenAndCode();

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "code", "000000"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
        }

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL, "code", "000000"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));
    }

    @Test
    void resendAlwaysReturnsTheSameGenericMessageRegardlessOfWhetherTheEmailExists() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "no-such-account@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        registerAndCaptureRawTokenAndCode();
        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resendIsRateLimitedAfterThreePerHour() throws Exception {
        registerAndCaptureRawTokenAndCode();

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/auth/resend-verification")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(Map.of("email", EMAIL))))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", EMAIL))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }
}
