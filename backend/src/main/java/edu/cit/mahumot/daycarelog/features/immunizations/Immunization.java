package edu.cit.mahumot.daycarelog.features.immunizations;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// No DB-level unique constraint on (child_id, vaccine_name, dose_number) here: soft-deleted
// rows keep occupying that combination, which would block re-adding a dose after it's
// trashed. Uniqueness among *active* records is enforced in ImmunizationService.create()
// instead (see the partial unique index note in V4__soft_delete_health_and_immunizations.sql
// for the DB-level equivalent, which is deleted_at-aware).
@Entity
@Table(name = "immunizations")
public class Immunization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(name = "vaccine_name", nullable = false)
    private String vaccineName;

    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Column(name = "date_given", nullable = false)
    private LocalDate dateGiven;

    @Column(name = "administered_by")
    private String administeredBy;

    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Immunization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }

    public LocalDate getDateGiven() { return dateGiven; }
    public void setDateGiven(LocalDate dateGiven) { this.dateGiven = dateGiven; }

    public String getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(String administeredBy) { this.administeredBy = administeredBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long childId;
        private String vaccineName;
        private Integer doseNumber;
        private LocalDate dateGiven;
        private String administeredBy;
        private String notes;

        public Builder childId(Long v) { this.childId = v; return this; }
        public Builder vaccineName(String v) { this.vaccineName = v; return this; }
        public Builder doseNumber(Integer v) { this.doseNumber = v; return this; }
        public Builder dateGiven(LocalDate v) { this.dateGiven = v; return this; }
        public Builder administeredBy(String v) { this.administeredBy = v; return this; }
        public Builder notes(String v) { this.notes = v; return this; }

        public Immunization build() {
            Immunization i = new Immunization();
            i.childId = this.childId;
            i.vaccineName = this.vaccineName;
            i.doseNumber = this.doseNumber;
            i.dateGiven = this.dateGiven;
            i.administeredBy = this.administeredBy;
            i.notes = this.notes;
            return i;
        }
    }
}
