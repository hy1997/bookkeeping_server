package com.bookkeeping.model;


import jakarta.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double dailyBudget;
    @ElementCollection
    @CollectionTable(name = "monthly_budgets", joinColumns = @JoinColumn(name = "budget_id"))
    @MapKeyColumn(name = "month")
    @Column(name = "amount")
    private Map<String, Double> monthlyBudgets = new HashMap<>();
    private Integer notificationThreshold = 80; // 默认80%时提醒
    private Boolean notificationEnabled = true; // 默认启用提醒

    @ElementCollection
    @CollectionTable(name = "daily_budgets", joinColumns = @JoinColumn(name = "budget_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "amount")
    private Map<String, Double> dailyBudgets = new HashMap<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private List<FixedExpense> fixedExpenses;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getDailyBudget() { return dailyBudget; }
    public void setDailyBudget(Double dailyBudget) { this.dailyBudget = dailyBudget; }
    public Map<String, Double> getMonthlyBudgets() { return monthlyBudgets; }
    public void setMonthlyBudgets(Map<String, Double> monthlyBudgets) { this.monthlyBudgets = monthlyBudgets; }
    public List<FixedExpense> getFixedExpenses() { return fixedExpenses; }
    public void setFixedExpenses(List<FixedExpense> fixedExpenses) { this.fixedExpenses = fixedExpenses; }
    public Map<String, Double> getDailyBudgets() { return dailyBudgets; }
    public void setDailyBudgets(Map<String, Double> dailyBudgets) { this.dailyBudgets = dailyBudgets; }
    public Integer getNotificationThreshold() { return notificationThreshold; }
    public void setNotificationThreshold(Integer notificationThreshold) { this.notificationThreshold = notificationThreshold; }
    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
} 