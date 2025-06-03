package com.bookkeeping.controller;

import com.bookkeeping.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.bookkeeping.service.UserService;
import com.bookkeeping.service.BudgetService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final BudgetService budgetService;

    public UserController(UserService userService, BudgetService budgetService) {
        this.userService = userService;
        this.budgetService = budgetService;
    }

    @PostMapping("/bind")
    public ResponseEntity<String> bindUser(
            @RequestParam String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        User targetUser = userService.findByUsername(username);

        if (targetUser == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        if (currentUser.getBoundUser() != null) {
            return ResponseEntity.badRequest().body("您已绑定其他用户");
        }

        if (targetUser.getBoundUser() != null) {
            return ResponseEntity.badRequest().body("该用户已被其他用户绑定");
        }

        currentUser.setBoundUser(targetUser.getId());
        userService.save(currentUser);

        return ResponseEntity.ok("绑定成功");
    }

} 