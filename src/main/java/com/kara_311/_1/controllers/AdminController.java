package com.kara_311._1.controllers;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.kara_311._1.services.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Controllers {

    private UserServicesImpl userServicesImpl;

    @Autowired
    public void setUserServices(UserServicesImpl userServicesImpl) {
        this.userServicesImpl = userServicesImpl;
    }


    @GetMapping(value = "/")
    public String getAllUsersMainPage(ModelMap model) {

        List <User> users = userServicesImpl.getAllUsers();
        model.addAttribute("users", users);

        return "users";
    }

    @GetMapping(value = "/user")
    public String printCurrentUserInfo(ModelMap model, HttpServletRequest request) {



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String uri = request.getRequestURL().toString();

        if (uri.contains("/admin")) {
            model.addAttribute("tab", "admin");
        } else if (uri.contains("/user")) {
            model.addAttribute("tab", "user");
        }

        User currentUser = userServicesImpl.getCurrentUser(authentication);
        model.addAttribute("currentUser", currentUser);

        return "user";
    }

    @GetMapping(value = "/admin")
    public String getAllUsersAdminPage(ModelMap model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String newUser = request.getParameter("newUser");
        String update = request.getParameter("update");
        String delete = request.getParameter("delete");

        String uri = request.getRequestURL().toString();

        if (uri.contains("/admin")) {
            model.addAttribute("tab", "admin");
        } else if (uri.contains("/user")) {
            model.addAttribute("tab", "user");
        }


        User currentUser = userServicesImpl.getCurrentUser(authentication);
        model.addAttribute("currentUser", currentUser);

        if (newUser != null) {

            model.addAttribute("message", "Пользователь добавлен");
            userServicesImpl.createNewUser(model, request);

        }

        else if (update != null) {

            Long idParam = Long.parseLong(request.getParameter("id"));
            String name = request.getParameter("name");
            String lastName = request.getParameter("lastName");
            String ageParam = request.getParameter("age");
            String passwordParam = request.getParameter("password");

            String[] rolesArray = request.getParameterValues("roles");

            String rolesParam = String.join(",", rolesArray);

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

        }

        else if (delete != null) {

            String idDelete = request.getParameter("id");
            String result = userServicesImpl.deleteUser(Long.parseLong(idDelete)) == 1 ? "Пользователь удален" : "Пользователь не найден";

            model.addAttribute("message", result);
        }

        List <User> users = userServicesImpl.getAllUsers();
        model.addAttribute("users", users);
        return "admin";
    }



}