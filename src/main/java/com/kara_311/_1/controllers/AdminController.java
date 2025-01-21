package com.kara_311._1.controllers;

import com.kara_311._1.model.User;
import com.kara_311._1.model.Role;
import com.kara_311._1.services.UserServices;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AdminController {

    private final UserServices userServices;

    @Autowired
    public AdminController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        response.put("currentUser", userServices.getCurrentUser(auth));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllUsers(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        response.put("tab", userServices.determineTab(request));
        response.put("users", userServices.getAllUsers());
        response.put("currentUser", userServices.getCurrentUser(auth));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin")
    public ResponseEntity<Map<String, Object>> createUser(HttpServletRequest request) {
        ModelMap model = new ModelMap();
        userServices.createNewUser(model, request);

        if (model.containsAttribute("error")) {
            return ResponseEntity.badRequest().body(Map.of("error", model.getAttribute("error")));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", model.getAttribute("message"));
        response.put("user", model.getAttribute("user"));
        response.put("users", userServices.getAllUsers());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {

        String rolesAsString = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        userServices.updateUser(id, user.getUsername(), user.getLastName(), user.getAge(), user.getPassword(), rolesAsString);
        Map<String, Object> response = new HashMap<>();
        response.put("users", userServices.getAllUsers());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userServices.deleteUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("users", userServices.getAllUsers());
        return ResponseEntity.ok(response);
    }
}
