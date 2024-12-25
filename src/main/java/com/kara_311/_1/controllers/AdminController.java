package com.kara_311._1.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.kara_311._1.services.*;

import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AdminController {

    private UserServices userServices;

    @Autowired
    public void setUserServices(UserServicesImpl userServicesImpl) {
        this.userServices = userServicesImpl;
    }

    @GetMapping(value = "/")
    public String getAllUsersMainPage(ModelMap model) {
        model.addAttribute("users", userServices.getAllUsers());
        model.addAttribute("currentUser", userServices.getCurrentUser(SecurityContextHolder.getContext().getAuthentication()));
        return "users";
    }

    @GetMapping(value = "/user")
    public String printCurrentUserInfo(ModelMap model, HttpServletRequest request) {
        model.addAttribute("tab", userServices.determineTab(request));
        model.addAttribute("currentUser", userServices.getCurrentUser(SecurityContextHolder.getContext().getAuthentication()));
        return "user";
    }

    @GetMapping(value = "/admin")
    public String getAllUsersAdminPage(ModelMap model, HttpServletRequest request) {
        model.addAttribute("tab", userServices.determineTab(request));
        userServices.handleAdminActions(model, request);
        model.addAttribute("users", userServices.getAllUsers());
        model.addAttribute("currentUser", userServices.getCurrentUser(SecurityContextHolder.getContext().getAuthentication()));
        return "admin";
    }
}
