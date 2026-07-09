package edu.cit.mahumot.daycarelog.features.guardians;

import java.util.List;

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
