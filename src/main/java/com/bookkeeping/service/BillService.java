package com.bookkeeping.service;

import com.bookkeeping.model.Bill;
import com.bookkeeping.model.Category;
import com.bookkeeping.repository.BillRepository;
import com.bookkeeping.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BillService {
    private static final Logger logger = LoggerFactory.getLogger(BillService.class);
    
    @Autowired
    private BillRepository billRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Bill> getBillsByUserId(Long userId) {
        List<Bill> bills = billRepository.findByUserIdOrderByTimeDesc(userId);
        return enrichBillsWithCategoryInfo(bills, userId);
    }
    
    public List<Bill> getBillsByTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        logger.info("BillService.getBillsByTimeRange - 开始获取时间范围内的账单，用户ID: {}, 开始时间: {}, 结束时间: {}", userId, start, end);
        try {
            List<Bill> bills = billRepository.findByUserIdAndTimeBetweenOrderByTimeDesc(userId, start, end);
            logger.info("BillService.getBillsByTimeRange - 从Repository获取到原始账单数量: {}", bills.size());
            return enrichBillsWithCategoryInfo(bills, userId);
        } catch (Exception e) {
            logger.error("BillService.getBillsByTimeRange - 获取时间范围内的账单失败", e);
            throw new RuntimeException("获取时间范围内的账单失败: " + e.getMessage());
        }
    }
    
    public List<Bill> getMonthlyFixedBills(Long userId, LocalDateTime start, LocalDateTime end) {
        logger.info("开始获取月度固定账单，用户ID: {}, 开始时间: {}, 结束时间: {}", userId, start, end);
        try {
            List<Bill> bills = billRepository.findByUserIdAndTimeBetweenAndIsFixedOrderByTimeDesc(userId, start, end, true);
            logger.info("获取到月度固定账单数量: {}", bills.size());
            return enrichBillsWithCategoryInfo(bills, userId);
        } catch (Exception e) {
            logger.error("获取月度固定账单失败", e);
            throw new RuntimeException("获取月度固定账单失败: " + e.getMessage());
        }
    }
    
    public List<Bill> searchBills(Long userId, String keyword, LocalDateTime startTime, LocalDateTime endTime, Long categoryId, BigDecimal minAmount, BigDecimal maxAmount) {
        logger.info("开始搜索账单，用户ID: {}, 关键字: {}, 类别ID: {}, 开始时间: {}, 结束时间: {}", userId, keyword, categoryId, startTime, endTime);

        List<Bill> bills;

        // If time range is provided, use the repository method for efficient filtering
        if (startTime != null && endTime != null) {
            bills = billRepository.findByUserIdAndTimeBetweenOrderByTimeDesc(userId, startTime, endTime);
            logger.info("使用时间范围查询，获取到 {} 条账单。", bills.size());
        } else {
            // Otherwise, fetch all bills for the user (less efficient for large datasets)
            bills = billRepository.findByUserIdOrderByTimeDesc(userId);
            logger.info("未提供时间范围，获取到用户所有 {} 条账单。", bills.size());
        }

        // Apply other filters in memory if needed
        List<Bill> result = new ArrayList<>();

        for (Bill bill : bills) {
            boolean matches = true;

            if (keyword != null && !bill.getNote().toLowerCase().contains(keyword.toLowerCase())) {
                matches = false;
            }

            // Time range filtering is now primarily handled by the repository method,
            // but this check is still here for robustness or if startTime/endTime
            // was null initially and now some other filter was applied.
            if (startTime != null && bill.getTime().isBefore(startTime)) {
                matches = false;
            }

            if (endTime != null && bill.getTime().isAfter(endTime)) {
                matches = false;
            }

            if (categoryId != null && (bill.getCategoryId() == null || !bill.getCategoryId().equals(categoryId))) {
                matches = false;
            }

            // 金额过滤 (确保 bill.getAmount() 不为 null 再进行比较)
            if (minAmount != null && (bill.getAmount() == null || bill.getAmount().compareTo(minAmount) < 0)) {
                matches = false;
            }
            if (maxAmount != null && (bill.getAmount() == null || bill.getAmount().compareTo(maxAmount) > 0)) {
                matches = false;
            }

            if (matches) {
                result.add(bill);
            }
        }

        logger.info("搜索过滤后账单数量: {}", result.size());
        return enrichBillsWithCategoryInfo(result, userId); // Enrich bills before returning
    }
    
    @Transactional
    public Bill saveBill(Bill bill, Long userId) {
        // Check if updating existing bill and if user is authorized
        if (bill.getId() != null) {
            Optional<Bill> existingBill = billRepository.findById(bill.getId());
            if (existingBill.isEmpty() || !existingBill.get().getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized to save/update this bill");
            }
        }
        // Ensure categoryId is set, default to category ID 1 if null
        if (bill.getCategoryId() == null) {
            bill.setCategoryId(1L);
        }
        // Ensure userId is set for new bills
        if (bill.getUserId() == null || bill.getId() != null) {
            bill.setUserId(userId);
        }
         // Ensure the userId on the bill matches the requesting user for updates
         if (bill.getId() != null && !bill.getUserId().equals(userId)) {
             throw new RuntimeException("Unauthorized to save/update this bill: User ID mismatch");
         }
        return billRepository.save(bill);
    }
    
    @Transactional
    public Bill updateBill(Bill updatedBill, Long userId) {
        logger.info("Attempting to update bill with ID: {} for user ID: {}", updatedBill.getId(), userId);
        Optional<Bill> existingBillOptional = billRepository.findById(updatedBill.getId());
        
        if (existingBillOptional.isEmpty()) {
            logger.warn("Bill with ID: {} not found for update.", updatedBill.getId());
            throw new RuntimeException("Bill not found");
        }
        
        Bill existingBill = existingBillOptional.get();
        
        // Check if the requesting user is the owner of the bill
        if (!existingBill.getUserId().equals(userId)) {
            logger.warn("User {} attempted to update bill {} owned by {}. Unauthorized.", userId, updatedBill.getId(), existingBill.getUserId());
            throw new RuntimeException("Unauthorized to update this bill");
        }
        
        // Update relevant fields from updatedBill to existingBill
        // Note: Only update fields that are allowed to be modified by the user.
        // For example, prevent changing the userId or creation timestamp unexpectedly.
        existingBill.setTime(updatedBill.getTime());
        existingBill.setCategoryId(updatedBill.getCategoryId());
        existingBill.setAmount(updatedBill.getAmount());
        existingBill.setNote(updatedBill.getNote());
        existingBill.setIsFixed(updatedBill.getIsFixed());
        // Add other updatable fields here...
        
        Bill savedBill = billRepository.save(existingBill);
        logger.info("Bill with ID: {} updated successfully by user {}", savedBill.getId(), userId);
        return savedBill;
    }
    
    @Transactional
    public void deleteBill(Long id, Long userId) {
        Optional<Bill> billToDelete = billRepository.findById(id);
        if (billToDelete.isEmpty() || !billToDelete.get().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this bill");
        }
        billRepository.deleteById(id);
    }
    
    private List<Bill> enrichBillsWithCategoryInfo(List<Bill> bills, Long userId) {
        // Optimization: Fetch all categories for the user in one query
        Map<Long, Category> categoryMap = categoryRepository.findByUserIdOrderByOrderIndexAsc(userId).stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        for (Bill bill : bills) {
            if (bill.getCategoryId() != null) {
                Category category = categoryMap.get(bill.getCategoryId());
                if (category != null) {
                    bill.setCategoryName(category.getName());
                    bill.setCategoryIcon(category.getIcon());
                }
            }
        }
        return bills;
    }
} 