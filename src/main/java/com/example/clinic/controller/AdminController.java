package com.example.clinic.controller;

import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
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
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/users")
    public String viewAllUsers(@RequestParam(required = false) String email,
                               @RequestParam(required = false) Role role,
                               Model model) {
        List<User> users;

        if ((email != null && !email.isEmpty()) || role != null) {
            users = userRepository.findAll().stream()
                    .filter(u -> (email == null || u.getEmail().toLowerCase().contains(email.toLowerCase())))
                    .filter(u -> (role == null || u.getRole() == role))
                    .toList();
        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("email", email);
        model.addAttribute("role", role);

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

    @GetMapping("/appointments")
    public String showAppointmentsForm(@RequestParam(required = false) String doctorEmail, Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);
        model.addAttribute("selectedDoctorEmail", doctorEmail);
        model.addAttribute("appointment", new Appointment());

        if (doctorEmail != null && !doctorEmail.isEmpty()) {
            doctorRepository.findByEmail(doctorEmail).ifPresent(doctor -> {
                List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
                model.addAttribute("appointments", appointments);
            });
        }

        return "admin/appointments";
    }

    @PostMapping("/appointments")
    public String addAppointment(@ModelAttribute Appointment appointment,
                                 @RequestParam String doctorEmail) {
        doctorRepository.findByEmail(doctorEmail).ifPresent(doctor -> {
            appointment.setDoctor(doctor);
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setUser(null); // Пока без пациента
            appointmentRepository.save(appointment);
        });
        return "redirect:/admin/appointments?doctorEmail=" + doctorEmail;
    }


    @GetMapping("/doctors/{email}/edit")
    public String editDoctor(@PathVariable String email, Model model) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));
        model.addAttribute("doctor", doctor);
        return "admin/edit-doctor";
    }

    @PostMapping("/doctors/{email}/edit")
    public String updateDoctor(@PathVariable String email,
                               @ModelAttribute Doctor updatedDoctor) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        doctor.setSpecialization(updatedDoctor.getSpecialization());
        doctor.setSalary(updatedDoctor.getSalary());
        doctorRepository.save(doctor);
        return "redirect:/admin/users";
    }

}
