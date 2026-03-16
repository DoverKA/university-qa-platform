package org.wy.demo.entity;

public class LoginResponse {
    private Integer id;           // 改为 Integer
    private String username;
    private String role;
    private String token;

    // 构造方法参数也改为 Integer
    public LoginResponse(Integer id, String username, String role, String token) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.token = token;
    }

    // getter 和 setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}