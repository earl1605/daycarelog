package edu.cit.mahumot.daycarelog;

import edu.cit.mahumot.daycarelog.model.*;
import edu.cit.mahumot.daycarelog.repository.*;
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
class ParentRoleAccessControlTest {

    @LocalServerPort
    private int port;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private ChildRepository childRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private HealthRecordRepository healthRecordRepository;
    @Autowired private GuardianRepository guardianRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL  = "admin@test.daycarelog";
    private static final String PARENT_EMAIL = "parent@test.daycarelog";
    private static final String PASSWORD     = "TestPass123";

    private Long childAId; // linked to the parent
    private Long childBId; // NOT linked to the parent

    @BeforeEach
    void setUp() {
        guardianRepository.deleteAll();
        healthRecordRepository.deleteAll();
        attendanceRepository.deleteAll();
        childRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(User.builder()
                .email(ADMIN_EMAIL).password(passwordEncoder.encode(PASSWORD))
                .firstName("Test").lastName("Admin").role("admin").build());
        User parent = userRepository.save(User.builder()
                .email(PARENT_EMAIL).password(passwordEncoder.encode(PASSWORD))
                .firstName("Test").lastName("Parent").role("parent").build());

        Child childA = childRepository.save(Child.builder()
                .firstName("Alice").lastName("Cruz").dateOfBirth(LocalDate.of(2022, 1, 1))
                .sex("Female").enrollmentDate(LocalDate.of(2024, 1, 1)).enrollmentStatus("active")
                .createdBy(admin.getId()).build());
        Child childB = childRepository.save(Child.builder()
                .firstName("Bobby").lastName("Reyes").dateOfBirth(LocalDate.of(2022, 6, 1))
                .sex("Male").enrollmentDate(LocalDate.of(2024, 1, 1)).enrollmentStatus("active")
                .createdBy(admin.getId()).build());
        childAId = childA.getId();
        childBId = childB.getId();

        Guardian guardian = new Guardian();
        guardian.setChildId(childAId);
        guardian.setName("Test Parent");
        guardian.setRelationship("Mother");
        guardian.setUserId(parent.getId());
        guardianRepository.save(guardian);

        attendanceRepository.save(Attendance.builder().childId(childAId).date(LocalDate.of(2026, 6, 29)).status("present").build());
        attendanceRepository.save(Attendance.builder().childId(childBId).date(LocalDate.of(2026, 6, 29)).status("present").build());

        healthRecordRepository.save(HealthRecord.builder().childId(childAId).measurementDate(LocalDate.of(2026, 6, 1)).weightKg(new BigDecimal("11.5")).build());
        healthRecordRepository.save(HealthRecord.builder().childId(childBId).measurementDate(LocalDate.of(2026, 6, 1)).weightKg(new BigDecimal("12.0")).build());
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
    void parentMineEndpointsOnlyReturnTheirOwnLinkedChild() {
        String token = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(token);

        ResponseEntity<List> children = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(children.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(children.getBody()).hasSize(1);
        assertThat((Integer) ((Map) children.getBody().get(0)).get("id")).isEqualTo(childAId.intValue());

        ResponseEntity<List> attendance = restTemplate.exchange(
                url("/api/attendance/mine"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(attendance.getBody()).hasSize(1);
        assertThat((Integer) ((Map) attendance.getBody().get(0)).get("childId")).isEqualTo(childAId.intValue());

        ResponseEntity<List> health = restTemplate.exchange(
                url("/api/health-records/mine"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(health.getBody()).hasSize(1);
        assertThat((Integer) ((Map) health.getBody().get(0)).get("childId")).isEqualTo(childAId.intValue());
    }

    @Test
    void parentIsBlockedFromStaffOnlyListAndDirectChildIdEndpoints() {
        String token = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(token);

        // "list everything" endpoints must not leak other children's data to a parent
        assertThat(restTemplate.exchange(url("/api/children"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/attendance?date=2026-06-29"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        // direct by-ID access to a child NOT linked to this parent must also be blocked,
        // not just filtered out of "mine"
        assertThat(restTemplate.exchange(url("/api/children/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/attendance/child/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records/child/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        // even direct by-ID access to THEIR OWN linked child is blocked - parents only
        // ever go through /mine, which never accepts a child ID as input
        assertThat(restTemplate.exchange(url("/api/children/" + childAId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void parentCannotWriteAnything() {
        String token = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(token);

        Map<String, Object> newChild = Map.of("firstName", "Hacker", "lastName", "Kid",
                "dateOfBirth", "2023-01-01", "sex", "Male");
        assertThat(restTemplate.exchange(url("/api/children"), HttpMethod.POST, new HttpEntity<>(newChild, headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        List<Map<String, Object>> bulk = List.of(Map.of("childId", childAId, "date", "2026-06-30", "status", "present"));
        assertThat(restTemplate.exchange(url("/api/attendance/bulk"), HttpMethod.POST, new HttpEntity<>(bulk, headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        Map<String, Object> healthRecord = Map.of("childId", childAId, "measurementDate", "2026-06-30", "weightKg", 12);
        assertThat(restTemplate.exchange(url("/api/health-records"), HttpMethod.POST, new HttpEntity<>(healthRecord, headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        Map<String, Object> guardian = Map.of("name", "New Guardian");
        assertThat(restTemplate.exchange(url("/api/children/" + childAId + "/guardians"), HttpMethod.POST, new HttpEntity<>(guardian, headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/children/" + childAId + "/guardians"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void selfRegistrationCannotProduceAParentAccount() {
        // register() hardcodes role=staff and RegisterRequest has no role field at all,
        // but assert the end-to-end behavior in case that ever changes.
        Map<String, Object> body = Map.of(
                "email", "wannabe-parent@test.daycarelog", "password", PASSWORD,
                "firstName", "Wanna", "lastName", "Be", "role", "parent");
        restTemplate.postForEntity(url("/api/auth/register"), body, Map.class);

        userRepository.findByEmail("wannabe-parent@test.daycarelog")
                .ifPresentOrElse(
                        u -> assertThat(u.getRole()).isEqualTo("staff"),
                        () -> { throw new AssertionError("expected the registered user to exist"); });
    }

    @Test
    void adminAddingGuardianWithPortalAccountCreatesAWorkingParentLogin() {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(adminToken);

        Map<String, Object> body = Map.of(
                "name", "New Mom", "relationship", "Mother", "createPortalAccount", true,
                "email", "newmom@test.daycarelog");
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url("/api/children/" + childBId + "/guardians"), HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        String tempPassword = (String) res.getBody().get("tempPassword");
        assertThat(tempPassword).isNotBlank();

        String parentToken = loginAndGetToken("newmom@test.daycarelog", tempPassword);
        HttpHeaders parentHeaders = authHeaders(parentToken);
        ResponseEntity<List> mine = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(parentHeaders), List.class);
        assertThat(mine.getBody()).hasSize(1);
        assertThat((Integer) ((Map) mine.getBody().get(0)).get("id")).isEqualTo(childBId.intValue());
    }

    @Test
    void linkingTheSameParentEmailToASecondChildReusesTheAccountInsteadOfDuplicating() {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(adminToken);

        // childA is already linked to PARENT_EMAIL via a "parent" role account (see setUp).
        // Linking childB to the same email must reuse that account, not create a duplicate.
        Map<String, Object> body = Map.of(
                "name", "Test Parent", "relationship", "Mother", "createPortalAccount", true,
                "email", PARENT_EMAIL);
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url("/api/children/" + childBId + "/guardians"), HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        // reusing an existing account never mints a new temp password
        assertThat((String) res.getBody().get("tempPassword")).isEmpty();
        assertThat(userRepository.findByEmail(PARENT_EMAIL)).hasValueSatisfying(u -> {}); // still exactly one account
        assertThat(userRepository.countByRole("parent")).isEqualTo(1);

        String parentToken = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        ResponseEntity<List> mine = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(authHeaders(parentToken)), List.class);
        assertThat(mine.getBody()).hasSize(2);
    }
}
