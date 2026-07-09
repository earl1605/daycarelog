package edu.cit.mahumot.daycarelog.features.auth;

public class VerifyEmailRequest {
    private String token;
    private String email;
    private String code;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
