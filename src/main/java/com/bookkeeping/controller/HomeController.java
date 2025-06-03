package com.bookkeeping.controller;

import com.bookkeeping.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    @Autowired
    private HomeService homeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHome(@RequestParam Long userId) {
        try {
            logger.info("获取首页数据，用户ID: {}", userId);
            Map<String, Object> data = homeService.getHomeData(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            logger.info("获取首页数据成功，用户ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取首页数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取首页数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}