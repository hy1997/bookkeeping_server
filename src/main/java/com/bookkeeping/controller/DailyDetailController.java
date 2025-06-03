package com.bookkeeping.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/daily-detail")
public class DailyDetailController {
    @GetMapping
    public Map<String, Object> getDailyDetail(@RequestParam Long userId, @RequestParam int year, @RequestParam int month) {
        Map<String, Object> result = new HashMap<>();
        result.put("monthTotal", 2500);
        result.put("monthBudget", 1500);
        List<Map<String, Object>> days = new ArrayList<>();
        for (int d = 1; d <= 30; d++) {
            Map<String, Object> day = new HashMap<>();
            day.put("day", d);
            day.put("budget", 50);
            List<Map<String, Object>> bills = new ArrayList<>();
            if (d % 2 == 0) {
                Map<String, Object> b1 = new HashMap<>();
                b1.put("category", "餐饮");
                b1.put("amount", 10 + d);
                b1.put("remark", "午餐");
                bills.add(b1);
                Map<String, Object> b2 = new HashMap<>();
                b2.put("category", "交通");
                b2.put("amount", 5 + d);
                b2.put("remark", "公交");
                bills.add(b2);
            }
            day.put("bills", bills);
            days.add(day);
        }
        result.put("days", days);
        return result;
    }
} 