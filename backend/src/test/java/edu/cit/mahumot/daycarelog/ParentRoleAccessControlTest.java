package edu.cit.mahumot.daycarelog;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.features.children.Child;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import edu.cit.mahumot.daycarelog.features.attendance.Attendance;
import edu.cit.mahumot.daycarelog.features.attendance.AttendanceRepository;
import edu.cit.mahumot.daycarelog.features.health.HealthRecord;
import edu.cit.mahumot.daycarelog.features.health.HealthRecordRepository;
import edu.cit.mahumot.daycarelog.features.guardians.Guardian;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianRepository;
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

    private Long childAId;
    private Long childBId;

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

        assertThat(restTemplate.exchange(url("/api/children"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/attendance?date=2026-06-29"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records"), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

        assertThat(restTemplate.exchange(url("/api/children/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/attendance/child/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);
        assertThat(restTemplate.exchange(url("/api/health-records/child/" + childBId), HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode().value()).isEqualTo(403);

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
    void adminAddingGuardianWithPortalAccountCreatesAWorkingParentLoginOnceVerified() {
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

        User newParent = userRepository.findByEmail("newmom@test.daycarelog").orElseThrow();
        assertThat(newParent.getEmailVerified()).isFalse();

        String parentToken = loginAndGetToken("newmom@test.daycarelog", tempPassword);
        HttpHeaders parentHeaders = authHeaders(parentToken);
        ResponseEntity<String> blocked = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(parentHeaders), String.class);
        assertThat(blocked.getStatusCode().value()).isEqualTo(403);
        assertThat(blocked.getBody()).contains("EMAIL_NOT_VERIFIED");

        newParent.setEmailVerified(true);
        userRepository.save(newParent);
        String verifiedToken = loginAndGetToken("newmom@test.daycarelog", tempPassword);
        ResponseEntity<List> mine = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(authHeaders(verifiedToken)), List.class);
        assertThat(mine.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(mine.getBody()).hasSize(1);
        assertThat((Integer) ((Map) mine.getBody().get(0)).get("id")).isEqualTo(childBId.intValue());
    }

    @Test
    void linkingTheSameParentEmailToASecondChildReusesTheAccountInsteadOfDuplicating() {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(adminToken);

        Map<String, Object> body = Map.of(
                "name", "Test Parent", "relationship", "Mother", "createPortalAccount", true,
                "email", PARENT_EMAIL);
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url("/api/children/" + childBId + "/guardians"), HttpMethod.POST,
                new HttpEntity<>(body, headers), new ParameterizedTypeReference<>() {});
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat((String) res.getBody().get("tempPassword")).isEmpty();
        assertThat(userRepository.findByEmail(PARENT_EMAIL)).hasValueSatisfying(u -> {});
        assertThat(userRepository.countByRole("parent")).isEqualTo(1);

        String parentToken = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        ResponseEntity<List> mine = restTemplate.exchange(
                url("/api/children/mine"), HttpMethod.GET, new HttpEntity<>(authHeaders(parentToken)), List.class);
        assertThat(mine.getBody()).hasSize(2);
    }

    @Test
    void guardianAccountDirectoryGroupsByPersonAndRemoveUnlinksWithoutDeletingTheLogin() {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, PASSWORD);
        HttpHeaders headers = authHeaders(adminToken);
        Long parentUserId = userRepository.findByEmail(PARENT_EMAIL).orElseThrow().getId();

        Map<String, Object> body = Map.of(
                "name", "Test Parent", "relationship", "Mother", "createPortalAccount", true,
                "email", PARENT_EMAIL);
        restTemplate.exchange(url("/api/children/" + childBId + "/guardians"), HttpMethod.POST,
                new HttpEntity<>(body, headers), String.class);

        ResponseEntity<List> list = restTemplate.exchange(
                url("/api/guardians"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(list.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(list.getBody()).hasSize(1);
        Map<String, Object> account = (Map<String, Object>) list.getBody().get(0);
        assertThat(account.get("email")).isEqualTo(PARENT_EMAIL);
        assertThat((List) account.get("children")).hasSize(2);

        ResponseEntity<String> remove = restTemplate.exchange(
                url("/api/guardians/user/" + parentUserId), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        assertThat(remove.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<List> afterRemove = restTemplate.exchange(
                url("/api/guardians"), HttpMethod.GET, new HttpEntity<>(headers), List.class);
        assertThat(afterRemove.getBody()).isEmpty();
        assertThat(userRepository.findById(parentUserId)).isPresent();
        assertThat(guardianRepository.findByUserId(parentUserId)).isEmpty();
    }

    @Test
    void parentIsBlockedFromTheGuardianAccountDirectory() {
        String token = loginAndGetToken(PARENT_EMAIL, PASSWORD);
        assertThat(restTemplate.exchange(url("/api/guardians"), HttpMethod.GET, new HttpEntity<>(authHeaders(token)), String.class)
                .getStatusCode().value()).isEqualTo(403);
    }
}
