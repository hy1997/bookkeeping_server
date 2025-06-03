package com.bookkeeping.service;

import com.bookkeeping.model.UserBinding;
import com.bookkeeping.repository.UserBindingRepository;
import com.bookkeeping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class UserBindingService {
    @Autowired
    private UserBindingRepository userBindingRepository;

    @Autowired
    private UserRepository userRepository;

    public UserBinding bindUser(Long userId, String boundUsername) {
        // Check if bound user exists
        var boundUser = userRepository.findByUsername(boundUsername)
            .orElseThrow(() -> new RuntimeException("Bound user not found"));

        // Check if binding already exists
        if (userBindingRepository.existsByUserIdAndBoundUserId(userId, boundUser.getId())) {
            throw new RuntimeException("User already bound");
        }

        // Create new binding
        UserBinding binding = new UserBinding();
        binding.setUserId(userId);
        binding.setBoundUserId(boundUser.getId());
        binding.setCreatedAt(LocalDateTime.now());

        return userBindingRepository.save(binding);
    }

    public List<UserBinding> getBoundUsers(Long userId) {
        return userBindingRepository.findByUserId(userId);
    }

    public List<Map<String, Object>> getBoundUsersWithUsername(Long userId) {
        List<UserBinding> boundUsers = userBindingRepository.findByUserId(userId);
        
        return boundUsers.stream().map(binding -> {
            Map<String, Object> boundUserInfo = new HashMap<>();
            boundUserInfo.put("boundUserId", binding.getBoundUserId());
            // Fetch username for the bound user ID
            userRepository.findById(binding.getBoundUserId()).ifPresent(boundUser -> {
                boundUserInfo.put("username", boundUser.getUsername());
            });
            return boundUserInfo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void removeBinding(Long userId, Long boundUserId) {
        // Check if binding exists before attempting deletion
        if (userBindingRepository.existsByUserIdAndBoundUserId(userId, boundUserId)) {
            userBindingRepository.deleteByUserIdAndBoundUserId(userId, boundUserId);
        } else {
            throw new RuntimeException("Binding not found");
        }
    }
} 