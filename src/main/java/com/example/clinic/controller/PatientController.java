package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.AppointmentStatus;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @GetMapping("/appointments/available")
    public String viewAvailableAppointments(Model model) {
        List<Appointment> availableAppointments = appointmentRepository.findByUserIsNullOrderByDateTimeAsc();
        model.addAttribute("appointments", availableAppointments);
        return "patient/available_appointments";
    }

    @PostMapping("/appointments/book/{id}")
    public String bookAppointment(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        String userEmail = principal.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        appointment.setUser(user);
        appointmentRepository.save(appointment);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return "redirect:/patient/my-appointments";
    }

    @GetMapping("/my-appointments")
    public String myAppointments(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Appointment> myAppointments = appointmentRepository.findByUser(user);
        model.addAttribute("appointments", myAppointments);
        return "patient/my_appointments";
    }

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (appointment.getUser() != null && appointment.getUser().getEmail().equals(user.getEmail())) {
            appointment.setUser(null);
            appointment.setStatus(AppointmentStatus.AVAILABLE);
            appointmentRepository.save(appointment);
        }
        return "redirect:/patient/my-appointments";
    }
}

