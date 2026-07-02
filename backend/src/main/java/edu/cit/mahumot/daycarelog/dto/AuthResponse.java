package edu.cit.mahumot.daycarelog.dto;

public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public static class UserDto {
        private Long id;
        private String email;
        private String fullName;
        private String firstName;
        private String lastName;
        private String middleName;
        private String suffix;
        private String role;
        private String profilePhoto;

        public UserDto(Long id, String email, String fullName, String firstName,
                       String lastName, String middleName, String suffix,
                       String role, String profilePhoto) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = middleName;
            this.suffix = suffix;
            this.role = role;
            this.profilePhoto = profilePhoto;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getMiddleName() { return middleName; }
        public String getSuffix() { return suffix; }
        public String getRole() { return role; }
        public String getProfilePhoto() { return profilePhoto; }
    }
}
