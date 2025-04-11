package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private String email;

    private String phone;
    private String omsPolicyNumber;
    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;
}

