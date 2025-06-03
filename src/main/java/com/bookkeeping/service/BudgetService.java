package com.bookkeeping.service;

import com.bookkeeping.model.Budget;
import com.bookkeeping.repository.BudgetRepository;
import com.bookkeeping.repository.UserBindingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserBindingRepository userBindingRepository;
    
    public Budget getBudgetByUserId(Long userId) {
        return budgetRepository.findByUserId(userId).orElse(null);
    }

    public Budget saveOrUpdateBudget(Budget budget) {
        Optional<Budget> existing = budgetRepository.findByUserId(budget.getUserId());
        if (existing.isPresent()) {
            Budget old = existing.get();
            old.setDailyBudget(budget.getDailyBudget());
            if (budget.getMonthlyBudgets() != null) {
                old.getMonthlyBudgets().putAll(budget.getMonthlyBudgets());
            }
            old.setFixedExpenses(budget.getFixedExpenses());
            return budgetRepository.save(old);
        } else {
            return budgetRepository.save(budget);
        }
    }

    public void setMonthlyBudgetForMonth(Long userId, String month, Double budget) {
        Budget userBudget = getBudgetByUserId(userId);
        if (userBudget == null) {
            userBudget = new Budget();
            userBudget.setUserId(userId);
        }
        userBudget.getMonthlyBudgets().put(month, budget);
        budgetRepository.save(userBudget);
    }

    public Double getMonthlyBudgetForMonth(Long userId, String month) {
        Budget userBudget = getBudgetByUserId(userId);
        if (userBudget != null && userBudget.getMonthlyBudgets() != null) {
            return userBudget.getMonthlyBudgets().getOrDefault(month, 0.0);
        }
        return 0.0;
    }

    public Map<String, Object> getCombinedBudget(Long userId, String month) {
        Map<String, Object> result = new HashMap<>();
        double totalMonthlyBudget = 0.0;
        double totalFixedExpenses = 0.0;
        double totalDailyBudget = 0.0;

        // Get user's own budget
        Budget userBudget = getBudgetByUserId(userId);
        if (userBudget != null) {
            totalMonthlyBudget += userBudget.getMonthlyBudgets().getOrDefault(month, 0.0);
            totalDailyBudget += userBudget.getDailyBudget() != null ? userBudget.getDailyBudget() : 0.0;
            if (userBudget.getFixedExpenses() != null) {
                totalFixedExpenses += userBudget.getFixedExpenses().stream()
                    .mapToDouble(expense -> expense.getAmount())
                    .sum();
            }
        }

        // Get bound users' budgets
        List<Long> boundUserIds = userBindingRepository.findByUserId(userId).stream()
            .map(binding -> binding.getBoundUserId())
            .toList();

        for (Long boundUserId : boundUserIds) {
            Budget boundUserBudget = getBudgetByUserId(boundUserId);
            if (boundUserBudget != null) {
                totalMonthlyBudget += boundUserBudget.getMonthlyBudgets().getOrDefault(month, 0.0);
                totalDailyBudget += boundUserBudget.getDailyBudget() != null ? boundUserBudget.getDailyBudget() : 0.0;
                if (boundUserBudget.getFixedExpenses() != null) {
                    totalFixedExpenses += boundUserBudget.getFixedExpenses().stream()
                        .mapToDouble(expense -> expense.getAmount())
                        .sum();
                }
            }
        }

        result.put("monthlyBudget", totalMonthlyBudget);
        result.put("dailyBudget", totalDailyBudget);
        result.put("fixedExpenses", totalFixedExpenses);
        return result;
    }
} 