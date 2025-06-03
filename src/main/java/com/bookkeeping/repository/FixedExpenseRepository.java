package com.bookkeeping.repository;

import com.bookkeeping.model.FixedExpense;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface FixedExpenseRepository extends JpaRepository<FixedExpense, Long> {
} 