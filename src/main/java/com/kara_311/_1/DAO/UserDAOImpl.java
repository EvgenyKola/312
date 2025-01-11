package com.kara_311._1.DAO;

import com.kara_311._1.model.Role;
import com.kara_311._1.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserDAOImpl implements UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT user FROM User user", User.class).getResultList();
    }

    @Override
    public Integer deleteUser(Long userId) {
        return entityManager.createQuery("DELETE FROM User u WHERE u.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void createUser(User user) {
        entityManager.persist(user);
    }

    @Override
    public void editUser(User user) {
        entityManager.merge(user);
    }

    @Override
    public User getUser(Long userId) {
        return entityManager.find(User.class, userId);
    }

    @Override
    public List<Role> getAllRoles() {
        return entityManager.createQuery("SELECT r FROM Role r", Role.class).getResultList();
    }

    @Override
    public User updateUser(Long id, String name, String lastName, Byte age, String password, String roles) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден: " + id);
        }

        user.setUsername(name);
        user.setLastName(lastName);
        user.setAge(age);
        user.setPassword(password);

        if (roles != null) {
            Set<Role> roleSet = Arrays.stream(roles.split(","))
                    .map(roleName -> {
                        Role role = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                                .setParameter("name", roleName)
                                .getResultStream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));
                        return role;
                    })
                    .collect(Collectors.toSet());
            user.setRoles(roleSet);
        }

        entityManager.merge(user);
        return user;
    }

    @Override
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