package com.bookkeeping.repository;

import com.bookkeeping.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserIdOrderByTimeDesc(Long userId);
    
    List<Bill> findByUserIdAndTimeBetweenOrderByTimeDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    List<Bill> findByUserIdAndTimeBetweenAndIsFixedOrderByTimeDesc(Long userId, LocalDateTime startTime, LocalDateTime endTime, Boolean isFixed);

 }