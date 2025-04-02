package com.example.clinic.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name="doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private LocalDate birthDate;
    private Integer Salary;

    public Doctor() {}

    public Doctor(Long id, String fullName, String email, String phone, String specialization, LocalDate birthDate, Integer salary) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.specialization = specialization;
        this.birthDate = birthDate;
        Salary = salary;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Integer getSalary() {
        return Salary;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setSalary(Integer salary) {
        Salary = salary;
    }
}
