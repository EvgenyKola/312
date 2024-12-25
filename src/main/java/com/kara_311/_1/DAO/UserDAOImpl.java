package com.kara_311._1.DAO;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import com.kara_311._1.repositories.RoleRepository;
import com.kara_311._1.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Repository
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository role;

    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT user FROM User user", User.class).getResultList();
    }

    public Integer deleteUser(Long userId) {

        return entityManager.createQuery("DELETE FROM User u WHERE u.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void createUser(User user) {
        entityManager.persist(user);
    };

    public void editUser(User user) {
        entityManager.merge(user);
    };

    public User getUser(Long userId) {
        return entityManager.find(User.class, userId);
    };

    public List<Role> getAllRoles() {
        return entityManager.createQuery("SELECT r FROM Role r", Role.class).getResultList();
    }

    public User updateUser(Long id, String name, String lastName, Byte age, String password, String roles) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));

        user.setUsername(name);
        user.setLastName(lastName);
        user.setAge(age);
        user.setPassword(password);

        if (roles != null) {
            Set<Role> roleSet = Arrays.stream(roles.split(","))
                    .map(roleName -> role.findByName(roleName)
                            .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roleSet);
        }

        userRepository.save(user);
        return user;
    }

    public User findByUsername(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
