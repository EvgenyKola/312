package com.kara_311._1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String lastName;

    @Column
    private Byte age;

    @Column
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_roles", // Таблица соединения
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Override
    public String toString() {
        return username + " " + lastName + " " + age;
    }
}
