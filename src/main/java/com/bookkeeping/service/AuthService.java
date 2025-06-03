package com.bookkeeping.service;

import com.bookkeeping.model.User;
import com.bookkeeping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> register(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "用户名已存在");
            return response;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(user);
        response.put("success", true);
        response.put("message", "注册成功");
        response.put("data", Map.of("id", user.getId()));
        return response;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return response;
        }
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("user", user);
        return response;
    }
} 