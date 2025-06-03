package com.bookkeeping.repository;

import com.bookkeeping.model.UserBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserBindingRepository extends JpaRepository<UserBinding, Long> {
    List<UserBinding> findByUserId(Long userId);
    boolean existsByUserIdAndBoundUserId(Long userId, Long boundUserId);
    
    // Add method to delete by user and bound user ID
    void deleteByUserIdAndBoundUserId(Long userId, Long boundUserId);
} 