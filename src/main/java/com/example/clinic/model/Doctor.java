package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="doctors")
public class Doctor {

    @Id
    private String email;

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    private User user;

    private String fullName;
    private String phone;
    private String specialization;
    private String photoPath;

    private LocalDate birthDate;
    private String education;
    private LocalDate workStartDate;
    private String description;

    private Integer Salary;

    public int getExperience() {
        if (workStartDate == null) return 0;
        return Period.between(workStartDate, LocalDate.now()).getYears();
    }
}
