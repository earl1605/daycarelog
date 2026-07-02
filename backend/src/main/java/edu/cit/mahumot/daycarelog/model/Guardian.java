package edu.cit.mahumot.daycarelog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "guardians")
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id", nullable = false)
    private Long childId;

    @Column(nullable = false)
    private String name;

    private String relationship;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    // Nullable: a guardian row can exist as contact-only info with no portal login.
    // When set, links this guardian to a "parent" User account (users.id) that may
    // view this child's attendance/health records. One guardian row per (user, child)
    // pair — a parent linked to multiple children just gets multiple rows sharing
    // the same userId, reusing this table rather than adding a new join table.
    @Column(name = "user_id")
    private Long userId;

    public Guardian() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
