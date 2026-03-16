package org.wy.demo.entity;

import javax.management.relation.Role;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
    private String email;
    private String role;
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
