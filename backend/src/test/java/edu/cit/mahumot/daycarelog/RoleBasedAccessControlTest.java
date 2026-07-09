package edu.cit.mahumot.daycarelog;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleBasedAccessControlTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@test.daycarelog";
    private static final String STAFF_EMAIL = "staff@test.daycarelog";
    private static final String PASSWORD = "TestPass123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .email(ADMIN_EMAIL).password(passwordEncoder.encode(PASSWORD))
                .firstName("Test").lastName("Admin").role("admin").build());
        userRepository.save(User.builder()
                .email(STAFF_EMAIL).password(passwordEncoder.encode(PASSWORD))
                .firstName("Test").lastName("Staff").role("staff").build());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String loginAndGetToken(String email, String password) {
        Map<String, Object> body = Map.of("email", email, "password", password);
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url("/api/auth/login"), HttpMethod.POST,
                new HttpEntity<>(body), new ParameterizedTypeReference<>() {});
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        return (String) res.getBody().get("token");
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void selfRegistrationCannotProduceAnAdminAccount() {
        long adminsBefore = userRepository.countByRole("admin");

        Map<String, Object> body = Map.of(
                "email", "wannabe-admin@test.daycarelog",
                "password", PASSWORD,
                "firstName", "Wanna", "lastName", "Be",
                "role", "admin");
        restTemplate.postForEntity(url("/api/auth/register"), body, Map.class);

        assertThat(userRepository.countByRole("admin")).isEqualTo(adminsBefore);
        userRepository.findByEmail("wannabe-admin@test.daycarelog")
                .ifPresent(u -> assertThat(u.getRole()).isEqualTo("staff"));
    }

    @Test
    void staffIsBlockedFromAdminOnlyUserManagementEndpoints() {
        String staffToken = loginAndGetToken(STAFF_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(staffToken);

        ResponseEntity<String> listUsers = restTemplate.exchange(
                url("/api/users"), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(listUsers.getStatusCode().value()).isEqualTo(403);

        Map<String, Object> createBody = Map.of("email", "x@test.daycarelog", "firstName", "X", "lastName", "Y", "role", "staff");
        ResponseEntity<String> create = restTemplate.exchange(
                url("/api/users"), HttpMethod.POST, new HttpEntity<>(createBody, headers), String.class);
        assertThat(create.getStatusCode().value()).isEqualTo(403);

        Long adminId = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow().getId();
        ResponseEntity<String> deactivate = restTemplate.exchange(
                url("/api/users/" + adminId + "/deactivate"), HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertThat(deactivate.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void adminHasFullAccessIncludingCreatingNewStaff() {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(adminToken);

        ResponseEntity<List> listUsers = restTemplate.exchange(
                url("/api/users"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(listUsers.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(listUsers.getBody()).hasSize(2);

        Map<String, Object> createBody = Map.of(
                "email", "newstaff@test.daycarelog", "firstName", "New", "lastName", "Staff", "role", "staff");
        ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
                url("/api/users"), HttpMethod.POST, new HttpEntity<>(createBody, headers),
                new ParameterizedTypeReference<>() {});
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        String tempPassword = (String) created.getBody().get("tempPassword");
        assertThat(tempPassword).isNotBlank();

        String newStaffToken = loginAndGetToken("newstaff@test.daycarelog", tempPassword);
        assertThat(newStaffToken).isNotBlank();
    }

    @Test
    void deactivationTakesEffectImmediatelyEvenWithAnExistingToken() {
        String staffToken = loginAndGetToken(STAFF_EMAIL, PASSWORD);

        ResponseEntity<String> before = restTemplate.exchange(
                url("/api/children"), HttpMethod.GET, new HttpEntity<>(authHeaders(staffToken)), String.class);
        assertThat(before.getStatusCode().is2xxSuccessful()).isTrue();

        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        Long staffId = userRepository.findByEmail(STAFF_EMAIL).orElseThrow().getId();
        ResponseEntity<String> deactivateRes = restTemplate.exchange(
                url("/api/users/" + staffId + "/deactivate"), HttpMethod.PUT,
                new HttpEntity<>(authHeaders(adminToken)), String.class);
        assertThat(deactivateRes.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<String> afterChildren = restTemplate.exchange(
                url("/api/children"), HttpMethod.GET, new HttpEntity<>(authHeaders(staffToken)), String.class);
        assertThat(afterChildren.getStatusCode().is2xxSuccessful()).isFalse();

        Map<String, Object> loginBody = Map.of("email", STAFF_EMAIL, "password", PASSWORD);
        ResponseEntity<String> loginAfter = restTemplate.postForEntity(url("/api/auth/login"), loginBody, String.class);
        assertThat(loginAfter.getStatusCode().is2xxSuccessful()).isFalse();
    }
}
