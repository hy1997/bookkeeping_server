package com.bookkeeping.controller;

import com.bookkeeping.model.Budget;
import com.bookkeeping.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);
    
    @Autowired
    private BudgetService budgetService;

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getBudget(@RequestParam Long userId) {
        try {
            Budget budget = budgetService.getBudgetByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", budget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取预算设置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取预算设置失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateBudget(@RequestBody Budget budget) {
        try {
            Budget updatedBudget = budgetService.saveOrUpdateBudget(budget);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBudget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新预算设置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新预算设置失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/monthly")
    public ResponseEntity<Map<String, Object>> setMonthlyBudget(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String month = (String) request.get("month");
            Double budget = ((Number) request.get("budget")).doubleValue();
            budgetService.setMonthlyBudgetForMonth(userId, month, budget);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "保存成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("设置月度预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置月度预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyBudget(@RequestParam Long userId, @RequestParam String month) {
        try {
            Double budget = budgetService.getMonthlyBudgetForMonth(userId, month);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "month", month,
                "budget", budget
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取月度预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取月度预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/daily/default")
    public ResponseEntity<Map<String, Object>> setDefaultDailyBudget(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Double budget = ((Number) request.get("budget")).doubleValue();
            
            Budget userBudget = budgetService.getBudgetByUserId(userId);
            if (userBudget == null) {
                userBudget = new Budget();
                userBudget.setUserId(userId);
            }
            userBudget.setDailyBudget(budget);
            Budget updatedBudget = budgetService.saveOrUpdateBudget(userBudget);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBudget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("设置默认每日预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置默认每日预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/daily")
    public ResponseEntity<Map<String, Object>> setDailyBudget(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            String dateStr = (String) request.get("date");
            Double budget = ((Number) request.get("budget")).doubleValue();
            Boolean setForWholeMonth = ((Boolean) request.get("setForWholeMonth")) ;

            Budget userBudget = budgetService.getBudgetByUserId(userId);
            if (userBudget == null) {
                userBudget = new Budget();
                userBudget.setUserId(userId);
            }
            if(setForWholeMonth) {
                // 解析输入日期
                LocalDate inputDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                // 获取年月信息
                int year = inputDate.getYear();
                int month = inputDate.getMonthValue();
                int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
                // 初始化 Map 并预分配空间
                Map<String, Double> budgetMap = new HashMap<>(daysInMonth);
                // 定义日期格式化器（输出格式为 yyyy-MM-dd）
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                // 填充 Map
                for (int day = 1; day <= daysInMonth; day++) {
                    LocalDate date = LocalDate.of(year, month, day);
                    String dateKey = date.format(formatter); // 格式化为字符串
                    budgetMap.put(dateKey, budget);
                }
                 userBudget.getDailyBudgets().putAll(budgetMap);
            }else{
                userBudget.getDailyBudgets().put(dateStr, budget);
            }
            Budget updatedBudget = budgetService.saveOrUpdateBudget(userBudget);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBudget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("设置每日预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置每日预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyBudget(
            @RequestParam Long userId, 
            @RequestParam String date) {
        try {
            Budget userBudget = budgetService.getBudgetByUserId(userId);
            double budgetAmount = 0;
            
            if (userBudget != null) {
                if (userBudget.getDailyBudgets().containsKey(date)) {
                    budgetAmount = userBudget.getDailyBudgets().get(date);
                } else if (userBudget.getDailyBudget() != null) {
                    budgetAmount = userBudget.getDailyBudget();
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "date", date,
                "budget", budgetAmount
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取每日预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取每日预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/notification")
    public ResponseEntity<Map<String, Object>> setBudgetNotification(@RequestBody Map<String, Object> request) {
        try {
            Long userId = ((Number) request.get("userId")).longValue();
            Integer threshold = ((Number) request.get("threshold")).intValue();
            Boolean enabled = (Boolean) request.get("enabled");
            
            Budget userBudget = budgetService.getBudgetByUserId(userId);
            if (userBudget == null) {
                userBudget = new Budget();
                userBudget.setUserId(userId);
            }
            
            userBudget.setNotificationThreshold(threshold);
            userBudget.setNotificationEnabled(enabled);
            Budget updatedBudget = budgetService.saveOrUpdateBudget(userBudget);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBudget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("设置预算提醒失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置预算提醒失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/combined")
    public ResponseEntity<Map<String, Object>> getCombinedBudget(
            @RequestParam Long userId,
            @RequestParam String month) {
        try {
            Map<String, Object> combinedBudget = budgetService.getCombinedBudget(userId, month);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", combinedBudget);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取组合预算失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取组合预算失败");
            return ResponseEntity.badRequest().body(response);
        }
    }
} 