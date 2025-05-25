package be.kdg.expensetracker.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    
    public AdminController(UserRepository userRepository, ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }
    
    @GetMapping
    public String adminDashboard(Model model) {
        List<User> users = userRepository.findAll();
        long totalExpenses = expenseRepository.count();
        
        model.addAttribute("users", users);
        model.addAttribute("totalExpenses", totalExpenses);
        
        return "admin/dashboard";
    }
} 