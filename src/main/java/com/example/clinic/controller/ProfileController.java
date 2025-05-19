// профиль пользователя

package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // просмотр собственного профиля
    @GetMapping("/profile")
    public String viewProfile(Model model) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);

        if (user != null) {
            List<Appointment> appointments = appointmentRepository.findByUser(user);
            model.addAttribute("user", user);
            model.addAttribute("appointments", appointments);
        }

        return "user/profile";
    }
}
