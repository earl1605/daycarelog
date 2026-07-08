package edu.cit.mahumot.daycarelog.features.guardians;

import java.util.List;

// One row per parent-portal-account guardian, aggregating every child that account
// is linked to (a single Guardian.userId can span multiple Guardian rows/children).
public class GuardianAccountResponse {
    private final Long userId;
    private final String name;
    private final String email;
    private final String contactNumber;
    private final String address;
    private final String relationship;
    private final List<ChildSummary> children;

    public GuardianAccountResponse(Long userId, String name, String email, String contactNumber,
                                    String address, String relationship, List<ChildSummary> children) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
        this.relationship = relationship;
        this.children = children;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress() { return address; }
    public String getRelationship() { return relationship; }
    public List<ChildSummary> getChildren() { return children; }

    public record ChildSummary(Long id, String firstName, String lastName) {}
}
