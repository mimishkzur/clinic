package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.model.Role;
import com.example.clinic.model.User;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;


    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{email}/role")
    public String changeUserRole(@PathVariable String email, @RequestParam Role role) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);

            if (role == Role.DOCTOR) {
                // Проверяем, есть ли уже такой врач
                doctorRepository.findByEmail(email).orElseGet(() -> {
                    // Создаем нового врача на основе информации из User
                    Doctor doctor = new Doctor();
                    doctor.setEmail(user.getEmail());
                    doctor.setFullName(user.getFullName());
                    doctor.setPhone(user.getPhone());
                    doctor.setSpecialization("Не указана");
                    doctor.setSalary(0); // Можно задать дефолт
                    doctorRepository.save(doctor);
                    return doctor;
                });
            }
        });
        return "redirect:/admin/users";
    }

}
