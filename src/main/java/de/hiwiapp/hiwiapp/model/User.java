package de.hiwiapp.hiwiapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING) // speichert ENUM als String in der DB
    @Column(nullable = false)
    private Role role; // unser ENUM: STUDENT, BETREUER, ADMIN
}
