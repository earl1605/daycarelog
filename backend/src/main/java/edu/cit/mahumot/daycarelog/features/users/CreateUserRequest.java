package edu.cit.mahumot.daycarelog.features.users;

public class CreateUserRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String suffix;
    private String role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
