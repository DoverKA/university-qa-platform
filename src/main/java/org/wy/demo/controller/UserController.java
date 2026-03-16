package org.wy.demo.controller; // 注意：包名可能是 org.wy.demo.controller，请根据实际情况调整

import org.springframework.stereotype.Controller;
import org.wy.demo.entity.LoginResponse;
import org.wy.demo.entity.User;
import org.wy.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        // 调用修改后的 service 方法，返回 User 对象或 null
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());

        if (user != null) {
            // 生成一个简单的 token（后续可替换为 JWT）
            String token = "token-" + user.getId();

            // 构建响应对象
            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),   // 确保 User 实体有 role 字段
                    token
            );
            return ResponseEntity.ok(response); // 200 OK
        } else {
            // 登录失败返回 401 状态码和错误信息
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
    }
}