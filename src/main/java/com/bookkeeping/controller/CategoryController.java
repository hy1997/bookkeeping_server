package com.bookkeeping.controller;

import com.bookkeeping.model.Category;
import com.bookkeeping.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCategories(@RequestParam Long userId) {
        logger.info("开始获取用户分类列表，用户ID: {}", userId);
        try {
            List<Category> categories = categoryService.getCategoriesByUserId(userId);
            logger.info("获取到分类数量: {}", categories.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", Map.of(
                "results", categories,
                "total", categories.size()
            ));
            
            logger.info("用户分类列表获取成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("获取用户分类列表失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取用户分类列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Map<String, Object> request) {
        try {
            // 打印整个请求体以帮助调试
            logger.info("收到创建分类请求: {}", request);
            
            // 检查请求体是否为空
            if (request == null) {
                logger.error("请求体为空");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请求体不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 尝试从不同可能的字段名获取 userId
            Long userId = null;
            if (request.containsKey("userId")) {
                Object userIdObj = request.get("userId");
                logger.info("从 userId 字段获取到用户ID: {}, 类型: {}", userIdObj, userIdObj != null ? userIdObj.getClass().getName() : "null");
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            } else if (request.containsKey("user_id")) {
                Object userIdObj = request.get("user_id");
                logger.info("从 user_id 字段获取到用户ID: {}", userIdObj);
                if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                }
            }
            
            // 如果仍未获取到 userId，返回错误
            if (userId == null) {
                logger.error("用户ID不能为空，请求体中无法找到 userId 或 user_id 字段");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户ID不能为空，请确保请求包含 userId 字段");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建 Category 对象并设置属性
            Category category = new Category();
            
            // 设置用户ID
            category.setUserId(userId);
            
            // 设置其他属性，并添加类型检查
            if (request.containsKey("name") && request.get("name") != null) {
                category.setName(request.get("name").toString());
            } else {
                // name 是必须的
                logger.error("分类名称不能为空");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "分类名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.containsKey("icon") && request.get("icon") != null) {
                category.setIcon(request.get("icon").toString());
            } else {
                // 设置默认图标
                category.setIcon("📝");
            }
            
            if (request.containsKey("type") && request.get("type") != null) {
                category.setType(request.get("type").toString());
            } else {
                // 设置默认类型
                category.setType("expense");
            }
            
            if (request.containsKey("description") && request.get("description") != null) {
                category.setDescription(request.get("description").toString());
            }
            
            if (request.containsKey("isFixed") && request.get("isFixed") != null) {
                Object isFixedObj = request.get("isFixed");
                if (isFixedObj instanceof Boolean) {
                    category.setIsFixed((Boolean) isFixedObj);
                } else if (isFixedObj instanceof String) {
                    category.setIsFixed(Boolean.parseBoolean((String) isFixedObj));
                }
            }
            
            if (request.containsKey("orderIndex") && request.get("orderIndex") != null) {
                Object orderIndexObj = request.get("orderIndex");
                if (orderIndexObj instanceof Number) {
                    category.setOrderIndex(((Number) orderIndexObj).intValue());
                } else if (orderIndexObj instanceof String) {
                    category.setOrderIndex(Integer.parseInt((String) orderIndexObj));
                }
            }
            
            logger.info("准备创建分类: {}", category);
            Category savedCategory = categoryService.createCategory(category);
            logger.info("分类创建成功: {}", savedCategory);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedCategory);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("创建分类失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建分类失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
        category.setId(id);
            Category updatedCategory = categoryService.updateCategory(category);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedCategory);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新分类失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新分类失败");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
        categoryService.deleteCategory(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "分类已删除");
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除分类失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除分类失败");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initDefaultCategories(@RequestBody Map<String, Object> request) {
        try {
            if (request == null || !request.containsKey("userId")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

        Long userId = ((Number) request.get("userId")).longValue();
            logger.info("收到初始化默认分类请求，用户ID: {}", userId);
        
            List<Category> categories = categoryService.initDefaultCategories(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
            response.put("data", Map.of(
                "results", categories,
                "total", categories.size()
            ));
            logger.info("初始化默认分类成功，用户ID: {}, 分类数量: {}", userId, categories.size());
        return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("初始化默认分类失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "初始化默认分类失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 