package com.bookkeeping.repository;

import com.bookkeeping.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByOrderIndexAsc(Long userId);
} 