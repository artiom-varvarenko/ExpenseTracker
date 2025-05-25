package be.kdg.expensetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseViewDto {
    private Integer id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private UserViewDto user;
    private List<CategoryViewDto> categories;
    
    // Constructors
    public ExpenseViewDto() {
    }
    
    public ExpenseViewDto(Integer id, String description, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public UserViewDto getUser() {
        return user;
    }
    
    public void setUser(UserViewDto user) {
        this.user = user;
    }
    
    public List<CategoryViewDto> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategoryViewDto> categories) {
        this.categories = categories;
    }
} 