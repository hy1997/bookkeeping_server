package com.bookkeeping.service;

import com.bookkeeping.model.Bill;
import com.bookkeeping.model.Budget;
import com.bookkeeping.repository.BillRepository;
import com.bookkeeping.repository.BudgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeService {
    private static final Logger logger = LoggerFactory.getLogger(HomeService.class);

    @Autowired
    private BillRepository billRepository;
    @Autowired
    private BudgetRepository budgetRepository;

    public Map<String, Object> getHomeData(Long userId) {
        logger.info("开始获取首页数据，用户ID: {}", userId);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 今日
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            List<Bill> todayBills = billRepository.findByUserIdAndTimeBetweenOrderByTimeDesc(userId, startOfDay, endOfDay);
            BigDecimal usedToday = todayBills.stream()
                .map(Bill::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            logger.info("今日账单数量: {}, 总金额: {}", todayBills.size(), usedToday);

            // 本月
            YearMonth ym = YearMonth.now();
            LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = ym.atEndOfMonth().plusDays(1).atStartOfDay();
            List<Bill> monthBills = billRepository.findByUserIdAndTimeBetweenOrderByTimeDesc(userId, startOfMonth, endOfMonth);
            BigDecimal usedMonth = monthBills.stream()
                .map(Bill::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            logger.info("本月账单数量: {}, 总金额: {}", monthBills.size(), usedMonth);

            // 预算
            Budget budget = budgetRepository.findByUserId(userId).orElse(null);
            logger.info("获取到预算设置: {}", budget != null);

            result.put("todayBills", todayBills);
            result.put("usedToday", usedToday);
            result.put("usedMonth", usedMonth);
            result.put("budget", budget);
            
            logger.info("首页数据获取完成");
            return result;
        } catch (Exception e) {
            logger.error("获取首页数据失败", e);
            throw new RuntimeException("获取首页数据失败: " + e.getMessage());
        }
    }
}
