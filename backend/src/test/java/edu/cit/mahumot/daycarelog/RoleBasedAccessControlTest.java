package edu.cit.mahumot.daycarelog;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.features.children.Child;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import edu.cit.mahumot.daycarelog.features.health.HealthRecord;
import edu.cit.mahumot.daycarelog.features.health.HealthRecordRepository;
import edu.cit.mahumot.daycarelog.features.immunizations.Immunization;
import edu.cit.mahumot.daycarelog.features.immunizations.ImmunizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private ImmunizationRepository immunizationRepository;

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

    @Test
    void staffIsBlockedFromRecycleBinEndpointsEvenThoughTheyCanDelete() {
        String staffToken = loginAndGetToken(STAFF_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(staffToken);

        assertThat(restTemplate.exchange(url("/api/health-records/trash"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records/1/restore"), HttpMethod.PUT, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records/1/permanent"), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        assertThat(restTemplate.exchange(url("/api/immunizations/trash"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/immunizations/1/restore"), HttpMethod.PUT, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/immunizations/1/permanent"), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void adminCanSoftDeleteRestoreAndPermanentlyDeleteAHealthRecord() {
        Child child = childRepository.save(Child.builder()
                .firstName("Trash").lastName("Test").dateOfBirth(LocalDate.of(2023, 1, 1))
                .sex("Male").enrollmentStatus("active").build());
        HealthRecord record = healthRecordRepository.save(HealthRecord.builder()
                .childId(child.getId()).measurementDate(LocalDate.of(2026, 6, 1)).weightKg(new BigDecimal("11.0")).build());

        HttpHeaders headers = authHeaders(loginAndGetToken(ADMIN_EMAIL, PASSWORD));

        assertThat(restTemplate.exchange(url("/api/health-records/" + record.getId()), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<List> active = restTemplate.exchange(url("/api/health-records"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(active.getBody().stream().noneMatch(r -> ((Map) r).get("id").equals(record.getId().intValue()))).isTrue();

        ResponseEntity<List> trash = restTemplate.exchange(url("/api/health-records/trash"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(trash.getBody().stream().anyMatch(r -> ((Map) r).get("id").equals(record.getId().intValue()))).isTrue();

        assertThat(restTemplate.exchange(url("/api/health-records/" + record.getId() + "/restore"), HttpMethod.PUT, new HttpEntity<>(headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<List> activeAfterRestore = restTemplate.exchange(url("/api/health-records"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(activeAfterRestore.getBody().stream().anyMatch(r -> ((Map) r).get("id").equals(record.getId().intValue()))).isTrue();

        assertThat(restTemplate.exchange(url("/api/health-records/" + record.getId()), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(restTemplate.exchange(url("/api/health-records/" + record.getId() + "/permanent"), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<List> trashAfterPurge = restTemplate.exchange(url("/api/health-records/trash"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(trashAfterPurge.getBody().stream().noneMatch(r -> ((Map) r).get("id").equals(record.getId().intValue()))).isTrue();
    }

    @Test
    void restoringAnImmunizationDoseThatWasReRecordedIsRejected() {
        Child child = childRepository.save(Child.builder()
                .firstName("Dose").lastName("Conflict").dateOfBirth(LocalDate.of(2023, 1, 1))
                .sex("Female").enrollmentStatus("active").build());
        Immunization original = immunizationRepository.save(Immunization.builder()
                .childId(child.getId()).vaccineName("BCG").doseNumber(1).dateGiven(LocalDate.of(2026, 1, 1)).build());

        HttpHeaders headers = authHeaders(loginAndGetToken(ADMIN_EMAIL, PASSWORD));
        assertThat(restTemplate.exchange(url("/api/immunizations/" + original.getId()), HttpMethod.DELETE, new HttpEntity<>(headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        Map<String, Object> reRecorded = Map.of("childId", child.getId(), "vaccineName", "BCG", "doseNumber", 1, "dateGiven", "2026-06-01");
        assertThat(restTemplate.exchange(url("/api/immunizations"), HttpMethod.POST, new HttpEntity<>(reRecorded, headers), String.class)
                .getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<String> restoreAttempt = restTemplate.exchange(
                url("/api/immunizations/" + original.getId() + "/restore"), HttpMethod.PUT, new HttpEntity<>(headers), String.class);
        assertThat(restoreAttempt.getStatusCode().is2xxSuccessful()).isFalse();
        assertThat(restoreAttempt.getBody()).contains("already re-recorded");
    }
}
