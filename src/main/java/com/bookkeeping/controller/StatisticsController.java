package com.bookkeeping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bookkeeping.service.StatisticsService;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam Long userId) {
        try {
            logger.info("获取统计数据，用户ID: {}", userId);
            Map<String, Object> data = statisticsService.getStatistics(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            logger.info("获取统计数据成功，用户ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

        @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            logger.info("获取每日统计数据，用户ID: {}, 年: {}, 月: {}", userId, year, month);
            Map<String, Object> data = statisticsService.getDailyStatistics(userId, year, month);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            logger.info("获取每日统计数据成功，用户ID: {}, 年: {}, 月: {}", userId, year, month);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取每日统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取每日统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/day")
    public ResponseEntity<Map<String, Object>> getDayStatistics(
            @RequestParam Long userId,
            @RequestParam String date) {
        try {
            logger.info("获取日统计数据，用户ID: {}, 日期: {}", userId, date);
            Map<String, Object> data = statisticsService.getDayStatistics(userId, date);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            logger.info("获取日统计数据成功，用户ID: {}, 日期: {}", userId, date);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取日统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取日统计数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 