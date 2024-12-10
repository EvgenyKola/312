package com.kara_311._1.services;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import com.kara_311._1.DAO.UserDAO;
import com.kara_311._1.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserServicesImpl implements UserDetailsService {

    private UserRepository userRepository;

    private UserDAO userDao;

    @Autowired
    public UserServicesImpl(UserDAO userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), mapRolesToAuthority(user.getRoles()));
    }

    private Collection <? extends GrantedAuthority> mapRolesToAuthority(Collection<Role> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }

    @Transactional
    public User getUser(Long userId){
        return userDao.getUser(userId);
    };

    @Transactional
    public List<User> getAllUsers(){
        return userDao.getAllUsers();
    };

    @Transactional
    public Integer deleteUser(Long userId){
        return userDao.deleteUser(userId);
    };

    @Transactional
    public void editUser(User user) {
        userDao.editUser(user);
    };

    @Transactional
    public List<Role> getAllRoles(){
        return userDao.getAllRoles();
    };

    @Transactional
    public User updateUser (Long id, String name, String lastName, Byte age, String password, String roles) {
        return userDao.updateUser(id, name, lastName, age, password, roles);
    }

    @Transactional
    public String createNewUser (ModelMap model, HttpServletRequest request) {
        List <Role> allRoles = userDao.getAllRoles();
        model.addAttribute("allRoles", allRoles);

        if (request.getParameter("name") == null) {
            model.addAttribute("message", "Заполните поля");
            return "admin/create";
        }

        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String ageParam = request.getParameter("age");
        String password = request.getParameter("password");
        String role = request.getParameter("roles");

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
        return "admin/create";
    }

}
