package com.kara_311._1.services;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import com.kara_311._1.DAO.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServicesImpl implements UserDetailsService, UserServices {

    private UserDAO userDao;

    @Autowired
    public UserServicesImpl(UserDAO userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Transactional
    public User getUser(Long userId) {
        return userDao.getUser(userId);
    }

    @Transactional
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    @Transactional
    public Integer deleteUser(Long userId) {
        return userDao.deleteUser(userId);
    }

    @Transactional
    public void editUser(User user) {
        userDao.editUser(user);
    }

    @Transactional
    public List<Role> getAllRoles() {
        return userDao.getAllRoles();
    }

    @Transactional
    public User updateUser(Long id, String name, String lastName, Byte age, String password, String roles) {
        return userDao.updateUser(id, name, lastName, age, password, roles);
    }

    @Transactional
    public void createNewUser(ModelMap model, HttpServletRequest request) {
        List<Role> allRoles = userDao.getAllRoles();
        model.addAttribute("allRoles", allRoles);

        if (request.getParameter("name") == null) {
            model.addAttribute("message", "Заполните поля");
            return;
        }

        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String ageParam = request.getParameter("age");
        String password = request.getParameter("password");
        String[] rolesArray = request.getParameterValues("roles");

        String role = String.join(",", rolesArray);

        try {
            Byte age = Byte.parseByte(ageParam);
            User user = new User();
            user.setAge(age);
            user.setUsername(name);
            user.setLastName(lastName);
            user.setPassword(password);

            Set<Role> roles = Arrays.stream(role.split(","))
                    .map(roleName -> allRoles.stream()
                            .filter(r -> r.getName().equals(roleName))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName)))
                    .collect(Collectors.toSet());

            user.setRoles(roles);

            userDao.createUser(user);

            model.addAttribute("user", user);
            model.addAttribute("message", "Пользователь создан");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
    }

    @Transactional
    public String determineTab(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        return uri.contains("/admin") ? "admin" : "user";
    }

    @Transactional
    public void handleAdminActions(ModelMap model, HttpServletRequest request) {
        String newUser = request.getParameter("newUser");
        String update = request.getParameter("update");
        String delete = request.getParameter("delete");

        if (newUser != null) {
            createNewUser(model, request);
            model.addAttribute("message", "Пользователь добавлен");
        } else if (update != null) {
            handleUpdateUser(model, request);
        } else if (delete != null) {
            handleDeleteUser(model, request);
        }
    }

    @Transactional
    public void handleUpdateUser(ModelMap model, HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("id"));
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String ageParam = request.getParameter("age");
        String password = request.getParameter("password");
        String[] rolesArray = request.getParameterValues("roles");

        List<Role> allRoles = getAllRoles();
        model.addAttribute("allRoles", allRoles);

        if (name == null) {
            model.addAttribute("user", getUser(id));
            return;
        }

        Set<Role> roles = Arrays.stream(rolesArray)
                .map(roleName -> allRoles.stream()
                        .filter(role -> role.getName().equals(roleName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName)))
                .collect(Collectors.toSet());

        User user = new User();
        user.setId(id);
        user.setAge(Byte.parseByte(ageParam));
        user.setUsername(name);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setPassword(password);

        editUser(user);
        model.addAttribute("user", getUser(id));
        model.addAttribute("message", "Пользователь обновлен");
    }

    @Transactional
    public void handleDeleteUser(ModelMap model, HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("id"));
        String result = deleteUser(id) == 1 ? "Пользователь удален" : "Пользователь не найден";
        model.addAttribute("message", result);
    }

    @Transactional
    public User getCurrentUser(Authentication authentication) {
        User currentUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                currentUser = findByUsername(((UserDetails) principal).getUsername());
            }
        }
        return currentUser;
    }
}
