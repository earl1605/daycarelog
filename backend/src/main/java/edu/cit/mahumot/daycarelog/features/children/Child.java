package edu.cit.mahumot.daycarelog.features.children;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "children")
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String sex;

    private String address;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "enrollment_status")
    private String enrollmentStatus = "active";

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Child() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String sex;
        private String address;
        private LocalDate enrollmentDate;
        private String enrollmentStatus = "active";
        private String allergies;
        private String medicalConditions;
        private String bloodType;
        private Long createdBy;

        public Builder firstName(String v) { this.firstName = v; return this; }
        public Builder lastName(String v) { this.lastName = v; return this; }
        public Builder dateOfBirth(LocalDate v) { this.dateOfBirth = v; return this; }
        public Builder sex(String v) { this.sex = v; return this; }
        public Builder address(String v) { this.address = v; return this; }
        public Builder enrollmentDate(LocalDate v) { this.enrollmentDate = v; return this; }
        public Builder enrollmentStatus(String v) { this.enrollmentStatus = v; return this; }
        public Builder allergies(String v) { this.allergies = v; return this; }
        public Builder medicalConditions(String v) { this.medicalConditions = v; return this; }
        public Builder bloodType(String v) { this.bloodType = v; return this; }
        public Builder createdBy(Long v) { this.createdBy = v; return this; }

        public Child build() {
            Child c = new Child();
            c.firstName = this.firstName;
            c.lastName = this.lastName;
            c.dateOfBirth = this.dateOfBirth;
            c.sex = this.sex;
            c.address = this.address;
            c.enrollmentDate = this.enrollmentDate;
            c.enrollmentStatus = this.enrollmentStatus;
            c.allergies = this.allergies;
            c.medicalConditions = this.medicalConditions;
            c.bloodType = this.bloodType;
            c.createdBy = this.createdBy;
            return c;
        }
    }
}
