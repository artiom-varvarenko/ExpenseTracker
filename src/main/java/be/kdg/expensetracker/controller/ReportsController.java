package be.kdg.expensetracker.controller;

import be.kdg.expensetracker.repository.ExpenseRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class ReportsController {
    
    private final ExpenseRepository expenseRepository;
    
    public ReportsController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @GetMapping
    public String reportsPage(Model model) {
        long totalExpenses = expenseRepository.count();
        BigDecimal totalAmount = expenseRepository.calculateTotalAmount();
        
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        
        return "reports/dashboard";
    }
} 