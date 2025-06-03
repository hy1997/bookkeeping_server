package com.bookkeeping.model;

import jakarta.persistence.*;


@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_fixed")
    private Boolean isFixed;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsFixed() { return isFixed; }
    public void setIsFixed(Boolean isFixed) { this.isFixed = isFixed; }
} 