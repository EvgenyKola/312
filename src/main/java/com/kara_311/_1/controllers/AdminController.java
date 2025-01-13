package com.kara_311._1.controllers;

import com.kara_311._1.model.User;
import com.kara_311._1.services.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final UserServices userServices;

    @Autowired
    public AdminController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping("/user")
    public String printCurrentUserInfo(ModelMap model, HttpServletRequest request) {
        model.addAttribute("tab", userServices.determineTab(request));
        model.addAttribute("currentUser", userServices.getCurrentUser(SecurityContextHolder.getContext().getAuthentication()));

        // Добавим роль в модель
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));

        return "user";
    }

    @GetMapping(value = "/admin")
    public String getAllUsersAdminPage(ModelMap model, HttpServletRequest request) {
        model.addAttribute("tab", userServices.determineTab(request));
        userServices.handleAdminActions(model, request);
        model.addAttribute("users", userServices.getAllUsers());
        model.addAttribute("currentUser", userServices.getCurrentUser(SecurityContextHolder.getContext().getAuthentication()));

        // Добавим роль в модель
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("isAdmin", auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")));

        return "admin";
    }
}

