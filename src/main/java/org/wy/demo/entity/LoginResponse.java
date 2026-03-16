package org.wy.demo.entity;

public class LoginResponse {
    private Integer id;
    private String username;
    private String role;
    private String token;
    private String avatarUrl;

    public LoginResponse(Integer id, String username, String role, String token, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
        this.avatarUrl = avatarUrl;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
