package com.kara_311._1.controllers;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.kara_311._1.services.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class Controllers {

    private UserServicesImpl userServicesImpl;

    @Autowired
    public void setUserServices(UserServicesImpl userServicesImpl) {
        this.userServicesImpl = userServicesImpl;
    }

    @GetMapping(value = "/admin/create")
    public String createNewUser(ModelMap model, HttpServletRequest request) {
        return userServicesImpl.createNewUser(model, request);
    }

    @GetMapping(value = "/admin")
    public String getAllUsersAdminPage(ModelMap model) {

        List <User> users = userServicesImpl.getAllUsers();
        model.addAttribute("users", users);

        return "admin";
    }

    @GetMapping(value = "/")
    public String getAllUsersMainPage(ModelMap model) {

        List <User> users = userServicesImpl.getAllUsers();
        model.addAttribute("users", users);

        return "users";
    }

    @GetMapping(value = "/user")
    public String printCurrentUserInfo(ModelMap model, HttpServletRequest request) {

        String update = request.getParameter("update");

        if (update != null) {

            Long id = Long.parseLong(request.getParameter("id"));
            String name = request.getParameter("name");
            String lastName = request.getParameter("lastName");
            String ageParam = request.getParameter("age");
            String password = request.getParameter("password");

            try {
                Byte age = Byte.parseByte(ageParam);
                User updatedUser = userServicesImpl.updateUser(id, name, lastName, age, password, null);
                model.addAttribute("user", updatedUser);
                model.addAttribute("message", "Ваши данные обновлены");
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
            return "user";
        }

        User currentUser = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = "Гость"; // Значение по умолчанию
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
                currentUser = userServicesImpl.findByUsername(username);
            }
        }

        model.addAttribute("user", currentUser);
        return "user";
    }

    @GetMapping(value = "/admin/delete")
    public String deleteUser(ModelMap model, HttpServletRequest request) {

        String idDelete = request.getParameter("id");
        String result = userServicesImpl.deleteUser(Long.parseLong(idDelete)) == 1 ? "Пользователь удален" : "Пользователь не найден";

        model.addAttribute("delete", result);

        return "admin/delete";
    }

    @GetMapping(value = "/admin/edit")
    public String editUser(ModelMap model, HttpServletRequest request) {
        Long idParam = Long.parseLong(request.getParameter("id"));
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String ageParam = request.getParameter("age");
        String passwordParam = request.getParameter("password");
        String rolesParam = request.getParameter("roles");

        List <Role> allRoles = userServicesImpl.getAllRoles();
        model.addAttribute("allRoles", allRoles);

        if (name == null) {
            model.addAttribute("user", userServicesImpl.getUser(idParam));
            return "admin/edit";
        }

        Set <Role> roles = Arrays.stream(rolesParam.split(","))
                .map(roleName -> allRoles.stream()
                        .filter(role -> role.getName().equals(roleName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName)))
                .collect(Collectors.toSet());

        User user = new User();
        user.setId(idParam);
        user.setAge(Byte.parseByte(ageParam));
        user.setUsername(name);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setPassword(passwordParam);

        userServicesImpl.editUser(user);
        
        User getUser = userServicesImpl.getUser(idParam);
        model.addAttribute("user", getUser);
        model.addAttribute("message", "Пользователь обновлен");

        return "admin/edit";
    }
}