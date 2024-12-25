package com.kara_311._1.DAO;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;

import java.util.List;

public interface UserDAO {
    public User getUser(Long userId);
    public List<User> getAllUsers();
    public Integer deleteUser(Long userId);
    public void createUser(User user);
    public void editUser(User user);
    public List<Role> getAllRoles();
    public User updateUser(Long id, String name, String lastName, Byte age, String password, String roles);
    public User findByUsername(String username);

}
