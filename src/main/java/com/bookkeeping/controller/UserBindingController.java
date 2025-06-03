package com.bookkeeping.controller;

import com.bookkeeping.service.UserBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/user-binding")
public class UserBindingController {
    @Autowired
    private UserBindingService userBindingService;

    @PostMapping("/bind")
    public ResponseEntity<Map<String, Object>> bindUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String boundUsername = (String) request.get("username");
            
            userBindingService.bindUser(userId, boundUsername);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User bound successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/bound-users")
    public ResponseEntity<Map<String, Object>> getBoundUsers(@RequestParam Long userId) {
        try {
            var boundUsers = userBindingService.getBoundUsersWithUsername(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", boundUsers != null ? boundUsers : new java.util.ArrayList<>());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeBinding(
            @RequestParam Long userId,
            @RequestParam Long boundUserId) {
        try {
            userBindingService.removeBinding(userId, boundUserId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Binding removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 