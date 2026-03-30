package org.wy.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.wy.demo.entity.User;
import org.wy.demo.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public String register(User user) {
        validateNewUser(user);
        String role = normalizeRole(user.getRole(), false);
        user.setRole(role);
        user.setUsername(user.getUsername().trim());
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Registration successful";
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    public User updateRole(Integer userId, String role, Integer operatorId) {
        User operator = getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        if (!"admin".equals(operator.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can change user roles");
        }

        User user = getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(normalizeRole(role, true));
        return userRepository.save(user);
    }

    public void deleteUser(Integer userId, Integer operatorId) {
        User operator = getUserById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));
        if (!"admin".equals(operator.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can delete users");
        }
        if (operatorId.equals(userId)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        userRepository.deleteById(userId);
    }

    public Map<String, Long> getUserStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "studentCount", userRepository.countByRole("student"),
                "teacherCount", userRepository.countByRole("teacher"),
                "adminCount", userRepository.countByRole("admin")
        );
    }

    private void validateNewUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("注册信息不能为空");
        }
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (userRepository.existsByUsername(user.getUsername().trim())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码至少需要 6 位字符");
        }
    }

    private String normalizeRole(String role, boolean allowAdmin) {
        if (!StringUtils.hasText(role)) {
            return "student";
        }
        String normalized = role.trim().toLowerCase();
        if ("student".equals(normalized) || "teacher".equals(normalized)) {
            return normalized;
        }
        if (allowAdmin && "admin".equals(normalized)) {
            return normalized;
        }
        return "student";
    }
}
