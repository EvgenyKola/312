
package com.kara_311._1.services;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import com.kara_311._1.repositories.UserRepository;
import com.kara_311._1.repositories.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServicesImpl implements UserDetailsService, UserServices {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    public UserServicesImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
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


    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Integer deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            userRepository.deleteById(userId);
            return 1;  // Пользователь удален
        }
        return 0;  // Пользователь не найден
    }

    public void editUser(User user) {
        userRepository.save(user);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public User updateUser(Long id, String name, String lastName, Byte age, String password, String roles) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(name);
            user.setLastName(lastName);
            user.setAge(age);
            user.setPassword(password);
            user.setRoles(getRolesFromNames(roles));
            return userRepository.save(user);
        }
        return null;
    }

    public void createNewUser(ModelMap model, HttpServletRequest request) {

        List<Role> allRoles = roleRepository.findAll();
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
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(getRolesFromNames(role));

            userRepository.save(user);

            model.addAttribute("user", user);
            model.addAttribute("message", "Пользователь создан");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
    }

    private Set<Role> getRolesFromNames(String rolesString) {
        Set<Role> roles = new HashSet<>();
        String[] roleNames = rolesString.split(",");
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    public String determineTab(HttpServletRequest request) {
        String uri = request.getRequestURL().toString();
        return uri.contains("/admin") ? "admin" : "user";
    }


    /*no usages*/
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

    public void handleDeleteUser(ModelMap model, HttpServletRequest request) {
        Long id = Long.parseLong(request.getParameter("id"));
        String result = deleteUser(id) == 1 ? "Пользователь удален" : "Пользователь не найден";
        model.addAttribute("message", result);
    }
    /*no usages*/

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

    public void createUser(User user) {
        userRepository.save(user);
    }
}
