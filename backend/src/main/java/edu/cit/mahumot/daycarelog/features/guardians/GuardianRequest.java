package edu.cit.mahumot.daycarelog.features.guardians;

public class GuardianRequest {
    private String name;
    private String relationship;
    private String contactNumber;
    private String address;
    private Boolean isPrimary;

    // Optional: when true, also creates a "parent" portal User account (using
    // email below) and links it to this guardian row via Guardian.userId.
    private Boolean createPortalAccount;
    private String email;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public Boolean getCreatePortalAccount() { return createPortalAccount; }
    public void setCreatePortalAccount(Boolean createPortalAccount) { this.createPortalAccount = createPortalAccount; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
