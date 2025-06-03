package com.bookkeeping.controller;

import com.bookkeeping.model.Bill;
import com.bookkeeping.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    
    @Autowired
    private BillService billService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bill>> getBillsByUserId(@PathVariable Long userId) {
        logger.info("Received request to get bills for user ID: {}", userId);
        try {
            List<Bill> bills = billService.getBillsByUserId(userId);
            logger.info("Successfully retrieved {} bills for user ID: {}", bills.size(), userId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            logger.error("Error getting bills for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Bill>> getBillsByTimeRange(
            Authentication authentication,
            @RequestParam String start,
            @RequestParam String end) {
        logger.info("Received request to get bills by time range for user: {}", authentication.getName());
        try {
            Long userId = (Long) authentication.getPrincipal();
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<Bill> bills = billService.getBillsByTimeRange(userId, startTime, endTime);
            logger.info("Successfully retrieved {} bills for user {} in time range", bills.size(), userId);
            return ResponseEntity.ok(bills);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date time parameters", e);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error getting bills by time range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/monthly-fixed")
    public ResponseEntity<List<Bill>> getMonthlyFixedBills(
            Authentication authentication,
            @RequestParam String start,
            @RequestParam String end) {
        logger.info("Received request to get monthly fixed bills for user: {}", authentication.getName());
        try {
            Long userId = (Long) authentication.getPrincipal();
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<Bill> bills = billService.getMonthlyFixedBills(userId, startTime, endTime);
            logger.info("Successfully retrieved {} monthly fixed bills for user {}", bills.size(), userId);
            return ResponseEntity.ok(bills);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date time parameters for monthly fixed bills", e);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error getting monthly fixed bills", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveBill(@RequestBody Bill bill, @RequestParam Long userId) {
        logger.info("Received request to save bill for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        try {
            Bill savedBill = billService.saveBill(bill, userId);
            response.put("success", true);
            response.put("message", "Bill saved successfully");
            response.put("bill", savedBill);
            logger.info("Bill saved successfully with ID: {}", savedBill.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error saving bill", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error saving bill", e);
            response.put("success", false);
            response.put("message", "Failed to save bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{billId}")
    public ResponseEntity<Map<String, Object>> updateBill(@PathVariable Long billId, @RequestBody Bill updatedBill, @RequestParam Long userId) {
        logger.info("Received request to update bill with ID: {} for user ID: {}", billId, userId);
        Map<String, Object> response = new HashMap<>();
        try {
            // Set the ID on the updatedBill object to ensure the correct bill is updated
            updatedBill.setId(billId);
            Bill resultBill = billService.updateBill(updatedBill, userId);
            response.put("success", true);
            response.put("message", "Bill updated successfully");
            response.put("bill", resultBill);
            logger.info("Bill with ID: {} updated successfully", billId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating bill with ID: {}", billId, e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error updating bill with ID: {}", billId, e);
            response.put("success", false);
            response.put("message", "Failed to update bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBill(@PathVariable Long id, @RequestParam Long userId) {
        logger.info("Received request to delete bill with ID: {} for user ID: {}", id, userId);
        Map<String, Object> response = new HashMap<>();
        try {
            billService.deleteBill(id, userId);
            response.put("success", true);
            response.put("message", "Bill deleted successfully");
            logger.info("Bill with ID: {} deleted successfully", id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error deleting bill with ID: {}", id, e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error deleting bill with ID: {}", id, e);
            response.put("success", false);
            response.put("message", "Failed to delete bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBills(
            @RequestParam Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {
        logger.info("Received request to search bills for user ID: {}", userId);
        Map<String, Object> response = new HashMap<>();
        try {
            // Use DateTimeFormatter for explicit parsing
            LocalDateTime start = startTime != null ? LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay() : null;
            LocalDateTime end = null;
            if (endTime != null) {
                // Parse the end time date using the formatter and set the time to the end of the day
                LocalDate endDateOnly = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE);
                end = endDateOnly.atTime(LocalTime.MAX); // Set time to the end of the day
            }

            List<Bill> bills = billService.searchBills(userId, keyword, start, end, categoryId, minAmount, maxAmount);

            // Calculate total amount
            BigDecimal totalAmount = bills.stream()
                    .map(Bill::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.put("success", true);
            response.put("data", Map.of(
                "results", bills,
                "total", totalAmount
            ));

            logger.info("Successfully retrieved {} search results for user {}, total amount: {}", bills.size(), userId, totalAmount);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date time parameters for search", e);
            response.put("success", false);
            response.put("message", "日期时间格式错误: " + e.getMessage() + ". 请确保日期格式为 yyyy-MM-dd");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error searching bills", e);
            response.put("success", false);
            response.put("message", "搜索账单失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyBills(
            @RequestParam Long userId,
            @RequestParam String date) {
        logger.info("开始获取日账单数据，用户ID: {}", userId);
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime start = localDate.atStartOfDay();
            LocalDateTime end = start.plusDays(1);

            logger.info("BillController.getDailyBills - 调用BillService获取日账单，用户ID: {}, 开始时间: {}, 结束时间: {}", userId, start, end);
            List<Bill> bills = billService.getBillsByTimeRange(userId, start, end);
            logger.info("BillController.getDailyBills - 从BillService获取到账单数量: {}", bills.size());
            
            double total = bills.stream()
                    .filter(b -> b.getIsFixed() == null || !b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            
            double fixedTotal = bills.stream()
                    .filter(b -> b.getIsFixed() != null && b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "date", date,
                "bills", bills,
                "total", total,
                "fixedTotal", fixedTotal,
                "grandTotal", total + fixedTotal
            ));
            
            logger.info("日账单数据获取成功，总支出: {}, 固定支出: {}, 总计: {}", 
                total, fixedTotal, total + fixedTotal);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取日账单数据失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取日账单数据失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/fixed/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyFixedBills(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            List<Bill> bills = billService.getMonthlyFixedBills(userId, startDateTime, endDateTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "results", bills,
                "total", bills.size()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取月度固定支出失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取月度固定支出失败");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
