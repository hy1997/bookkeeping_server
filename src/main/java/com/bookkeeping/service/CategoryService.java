package com.bookkeeping.service;

import com.bookkeeping.model.Category;
import com.bookkeeping.model.User;
import com.bookkeeping.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import com.bookkeeping.repository.UserRepository;

@Service
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Default categories data
    private static final List<Category> DEFAULT_CATEGORIES = List.of(
            createDefaultCategory("餐饮", "🍽️", "expense"),
            createDefaultCategory("购物", "🛍️", "expense"),
            createDefaultCategory("交通", "🚗", "expense"),
            createDefaultCategory("住房", "🏠", "expense"),
            createDefaultCategory("娱乐", "🎉", "expense"),
            createDefaultCategory("医疗", "🏥", "expense"),
            createDefaultCategory("教育", "📚", "expense"),
            createDefaultCategory("其他支出", "💡", "expense"),
            createDefaultCategory("工资", "💰", "income"),
            createDefaultCategory("兼职", "💼", "income"),
            createDefaultCategory("投资收益", "📈", "income"),
            createDefaultCategory("其他收入", "🌟", "income")
    );

    private static Category createDefaultCategory(String name, String icon, String type) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setType(type);
        category.setDescription(""); // 设置默认空描述
        category.setIsFixed(type.equals("expense") && (name.equals("住房") || name.equals("固定支出"))); // 住房和固定支出默认为固定支出
        // Default categories are not linked to a specific user initially
        return category;
    }

    // Initialize default categories if none exist for a user (example logic, needs refinement)
    @PostConstruct
    public void initializeDefaultCategoriesForExistingUsers() {
        // This is a basic example. In a real application, you'd do this on user creation
        // or first login to avoid iterating through all users on startup.
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (categoryRepository.findByUserIdOrderByOrderIndexAsc(user.getId()).isEmpty()) {
                DEFAULT_CATEGORIES.forEach(defaultCat -> {
                    Category userCategory = new Category();
                    userCategory.setName(defaultCat.getName());
                    userCategory.setIcon(defaultCat.getIcon());
                    userCategory.setType(defaultCat.getType());
                    userCategory.setDescription(defaultCat.getDescription());
                    userCategory.setIsFixed(defaultCat.getIsFixed());
                    userCategory.setUserId(user.getId());
                    categoryRepository.save(userCategory);
                });
            }
        }
    }

    public List<Category> getCategoriesByUserId(Long userId) {
        logger.info("开始获取用户分类列表，用户ID: {}", userId);
        try {
            List<Category> categories = categoryRepository.findByUserIdOrderByOrderIndexAsc(userId);
            logger.info("获取到分类数量: {}", categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("获取用户分类列表失败", e);
            throw new RuntimeException("获取用户分类列表失败: " + e.getMessage());
        }
    }
    
    public Category createCategory(Category category) {
        // 设置默认的排序索引
        if (category.getOrderIndex() == null) {
            List<Category> existingCategories = getCategoriesByUserId(category.getUserId());
            category.setOrderIndex(existingCategories.size());
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Category category) {
        // 验证分类是否存在
        if (!categoryRepository.existsById(category.getId())) {
            throw new RuntimeException("分类不存在");
        }
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
    
    public List<Category> initDefaultCategories(Long userId) {
        logger.info("开始为用户 {} 初始化默认分类", userId);
        
        // 检查用户是否已有分类
        List<Category> existingCategories = getCategoriesByUserId(userId);
        if (existingCategories != null && !existingCategories.isEmpty()) {
            logger.info("用户 {} 已有分类，跳过初始化", userId);
            return existingCategories;
        }
        
        // 创建默认分类
        List<Category> defaultCategories = new ArrayList<>();
        
        String[][] defaults = {
            {"餐饮", "🍔", "0"},
            {"交通", "🚕", "1"},
            {"购物", "🛒", "2"},
            {"居住", "🏠", "3"},
            {"通讯", "📱", "4"},
            {"娱乐", "🎮", "5"}
        };
        
        for (String[] data : defaults) {
            Category category = new Category();
            category.setUserId(userId);
            category.setName(data[0]);
            category.setIcon(data[1]);
            category.setOrderIndex(Integer.parseInt(data[2]));
            defaultCategories.add(category);
        }
        
        try {
            List<Category> savedCategories = categoryRepository.saveAll(defaultCategories);
            logger.info("用户 {} 的默认分类初始化成功，共 {} 个分类", userId, savedCategories.size());
            return savedCategories;
        } catch (Exception e) {
            logger.error("保存默认分类失败", e);
            throw new RuntimeException("保存默认分类失败: " + e.getMessage());
        }
    }
} 