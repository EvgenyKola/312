package com.kara_311._1.services;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.ui.ModelMap;

import java.util.List;

public interface UserServices {

    User findByUsername(String username);

    User getUser(Long userId);

    List<User> getAllUsers();

    Integer deleteUser(Long userId);

    void editUser(User user);

    List<Role> getAllRoles();

    User updateUser(Long id, String name, String lastName, Byte age, String password, String roles);

    void createNewUser(ModelMap model, HttpServletRequest request);

    User getCurrentUser(Authentication authentication);

    String determineTab(HttpServletRequest request);

    void createUser(User user);

    void handleAdminActions(ModelMap model, HttpServletRequest request);

}
