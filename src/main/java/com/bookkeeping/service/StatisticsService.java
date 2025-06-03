package com.bookkeeping.service;

import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.bookkeeping.model.Budget;
import com.bookkeeping.service.BillService;
import com.bookkeeping.service.BudgetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bookkeeping.model.Bill;

@Service
public class StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private BillService billService;
    @Autowired
    private BudgetService budgetService;

    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 2500);
        result.put("avg", 83);
        result.put("remain", 500);

        List<Map<String, Object>> categoryStats = new ArrayList<>();
        Map<String, Object> c1 = new HashMap<>();
        c1.put("category", "餐饮");
        c1.put("percent", 0.7);
        categoryStats.add(c1);
        Map<String, Object> c2 = new HashMap<>();
        c2.put("category", "交通");
        c2.put("percent", 0.2);
        categoryStats.add(c2);
        Map<String, Object> c3 = new HashMap<>();
        c3.put("category", "购物");
        c3.put("percent", 0.1);
        categoryStats.add(c3);
        result.put("categoryStats", categoryStats);

        List<Map<String, Object>> dailyStats = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("amount", Math.random() * 200);
            dailyStats.add(day);
        }
        result.put("dailyStats", dailyStats);
        return result;
    }

    public Map<String, Object> getDailyStatistics(Long userId, int year, int month) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> days = new ArrayList<>();
        Budget budget = budgetService.getBudgetByUserId(userId);
        java.time.YearMonth ym = java.time.YearMonth.of(year, month);
        int daysInMonth = ym.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = String.format("%04d-%02d-%02d", year, month, day);
            double budgetAmount = 0;
            if (budget != null && budget.getDailyBudgets() != null && budget.getDailyBudgets().containsKey(dateStr)) {
                budgetAmount = budget.getDailyBudgets().get(dateStr);
            } else if (budget != null && budget.getDailyBudget() != null) {
                budgetAmount = budget.getDailyBudget();
            }
            java.time.LocalDateTime start = java.time.LocalDateTime.of(year, month, day, 0, 0, 0);
            java.time.LocalDateTime end = start.plusDays(1);
            double spent = billService.getBillsByTimeRange(userId, start, end).stream()
                    .filter(b -> b.getIsFixed() == null || !b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            double fixedSpent = billService.getBillsByTimeRange(userId, start, end).stream()
                    .filter(b -> b.getIsFixed() != null && b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            double remain = budgetAmount - spent;
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", dateStr);
            dayStat.put("budget", budgetAmount);
            dayStat.put("spent", spent);
            dayStat.put("remain", remain);
            dayStat.put("dailyFixedSpent", fixedSpent);
            days.add(dayStat);
        }
        result.put("days", days);
        return result;
    }

    public Map<String, Object> getDayStatistics(Long userId, String date) {
        logger.info("开始获取日统计数据，用户ID: {}, 日期: {}", userId, date);
        try {
            Budget budget = budgetService.getBudgetByUserId(userId);
            double budgetAmount = 0;
            if (budget != null && budget.getDailyBudgets() != null && budget.getDailyBudgets().containsKey(date)) {
                budgetAmount = budget.getDailyBudgets().get(date);
            } else if (budget != null && budget.getDailyBudget() != null) {
                budgetAmount = budget.getDailyBudget();
            }
            logger.info("获取到预算金额: {}", budgetAmount);

            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            java.time.LocalDateTime start = localDate.atStartOfDay();
            java.time.LocalDateTime end = start.plusDays(1);
            
            List<Bill> bills = billService.getBillsByTimeRange(userId, start, end);
            logger.info("获取到账单数量: {}", bills.size());
            
            double spent = bills.stream()
                    .filter(b -> b.getIsFixed() == null || !b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            
            double fixedSpent = bills.stream()
                    .filter(b -> b.getIsFixed() != null && b.getIsFixed())
                    .mapToDouble(b -> b.getAmount() != null ? b.getAmount().doubleValue() : 0.0)
                    .sum();
            
            double remain = budgetAmount - spent;
            
            Map<String, Object> result = new HashMap<>();
            result.put("date", date);
            result.put("budget", budgetAmount);
            result.put("spent", spent);
            result.put("remain", remain);
            result.put("fixedSpent", fixedSpent);
            result.put("bills", bills);
            
            logger.info("日统计数据计算完成，预算: {}, 支出: {}, 剩余: {}, 固定支出: {}", 
                budgetAmount, spent, remain, fixedSpent);
            return result;
        } catch (Exception e) {
            logger.error("获取日统计数据失败", e);
            throw new RuntimeException("获取日统计数据失败: " + e.getMessage());
        }
    }
} 