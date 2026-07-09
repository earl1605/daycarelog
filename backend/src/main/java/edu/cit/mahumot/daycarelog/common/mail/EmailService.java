package edu.cit.mahumot.daycarelog.common.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");

    // No JavaMailSender bean exists unless spring.mail.host is configured, so this is
    // Optional rather than a hard dependency - local dev with MAIL_MODE=console needs
    // no SMTP configuration at all.
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public EmailService(Optional<JavaMailSender> mailSender, ObjectMapper objectMapper) {
        this.mailSender = mailSender.orElse(null);
        this.objectMapper = objectMapper;
    }

    @Value("${app.mail.mode:smtp}")
    private String mailMode;

    @Value("${app.mail.from:no-reply@daycarelog.local}")
    private String fromAddress;

    @Value("${app.web.base-url:http://localhost:5173}")
    private String webBaseUrl;

    // Raw SMTP (ports 25/465/587) is blocked outbound on some PaaS hosts (e.g. Railway),
    // so a configured connection just times out instead of failing cleanly. Resend's
    // HTTPS API (port 443) sidesteps that. Takes priority over SMTP whenever set; SMTP
    // stays as a fallback for hosts where the port genuinely isn't blocked.
    @Value("${app.email.resend-api-key:}")
    private String resendApiKey;

    public void sendVerificationEmail(String toEmail, String recipientName, String rawToken, String rawCode) {
        String link = webBaseUrl + "/verify-email?token=" + rawToken;
        String subject = "Verify your DaycareLog account";
        String html = buildHtml(recipientName, link, rawCode);

        if (!resendApiKey.isBlank()) {
            sendViaResend(toEmail, subject, html);
            return;
        }

        if (isConsoleMode()) {
            log.info(
                    "[MAIL_MODE=console] Verification email for {}\nSubject: {}\nLink: {}\nCode: {}",
                    toEmail, subject, link, rawCode
            );
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            // Verification email failing to send should never surface as a 500 to the
            // caller (registration/resend already succeeded) - it's logged so an admin
            // can investigate, and the user can always hit "Resend" again.
            log.error("Failed to send verification email to {}", toEmail, e);
        }
    }

    private void sendViaResend(String toEmail, String subject, String html) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "from", fromAddress,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "html", html
            ));
            HttpRequest request = HttpRequest.newBuilder(RESEND_ENDPOINT)
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                log.error("Resend API returned {} sending verification email to {}: {}",
                        response.statusCode(), toEmail, response.body());
            }
        } catch (Exception e) {
            // Same fail-soft policy as the SMTP path - never let a mail-provider outage
            // turn into a 500 on registration/resend.
            log.error("Failed to send verification email via Resend to {}", toEmail, e);
        }
    }

    private boolean isConsoleMode() {
        return "console".equalsIgnoreCase(mailMode) || mailSender == null;
    }

    private String buildHtml(String name, String link, String code) {
        String greeting = (name == null || name.isBlank()) ? "there" : name;
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background:#f4f6f6;font-family:Arial,Helvetica,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f6;padding:24px 0;">
                    <tr><td align="center">
                      <table width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;">
                        <tr><td style="background:#085454;padding:24px 32px;">
                          <span style="color:#ffffff;font-size:22px;font-weight:800;">DaycareLog</span>
                        </td></tr>
                        <tr><td style="padding:32px;">
                          <p style="font-size:16px;color:#111827;margin:0 0 16px;">Hi %s,</p>
                          <p style="font-size:14px;color:#374151;line-height:1.6;margin:0 0 24px;">
                            Please verify your email address to finish setting up your DaycareLog account.
                            Click the button below, or enter the 6-digit code if you're on the mobile app.
                          </p>
                          <table cellpadding="0" cellspacing="0" style="margin:0 0 28px;">
                            <tr><td style="background:#22c59a;border-radius:8px;">
                              <a href="%s" style="display:inline-block;padding:12px 28px;color:#085454;font-weight:700;font-size:14px;text-decoration:none;">
                                Verify my email
                              </a>
                            </td></tr>
                          </table>
                          <p style="font-size:13px;color:#6b7280;margin:0 0 8px;">Or enter this code in the app:</p>
                          <p style="font-family:'Courier New',monospace;font-size:32px;font-weight:700;letter-spacing:8px;color:#085454;background:#f0fdfa;border-radius:8px;padding:16px;text-align:center;margin:0 0 24px;">
                            %s
                          </p>
                          <p style="font-size:12px;color:#9ca3af;margin:0;">
                            The link expires in 24 hours and the code expires in 15 minutes.
                            If you didn't create a DaycareLog account, you can safely ignore this email.
                          </p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(greeting, link, code);
    }
}
