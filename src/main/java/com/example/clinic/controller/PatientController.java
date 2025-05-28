// функционал пациента

package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.AppointmentStatus;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    // просмотр всех доступных записей
    @GetMapping("/appointments/available")
    public String viewAvailableAppointments(@RequestParam(required = false) String specialization, Model model) {
        List<Appointment> availableAppointments = appointmentRepository.findByUserIsNullOrderByDateTimeAsc();

        List<String> specializations = availableAppointments.stream()
                .map(a -> a.getDoctor().getSpecialization())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (specialization != null && !specialization.isEmpty()) {
            availableAppointments = availableAppointments.stream()
                    .filter(app -> specialization.equals(app.getDoctor().getSpecialization()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("appointments", availableAppointments);
        model.addAttribute("specializations", specializations);
        model.addAttribute("selectedSpecialization", specialization);

        return "patient/available_appointments";
    }

    // запись на прием
    @PostMapping("/appointments/book/{id}")
    public String bookAppointment(@PathVariable Long id, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        String userEmail = principal.getName();
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        appointment.setUser(user);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        return "redirect:/patient/my-appointments";
    }

    // просмотр собственных записей
    @GetMapping("/my-appointments")
    public String myAppointments(@RequestParam(required = false) String specialization, Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        // Изменено на сортировку по убыванию даты
        List<Appointment> myAppointments = appointmentRepository.findByUserOrderByDateTimeDesc(user);

        List<String> specializations = myAppointments.stream()
                .map(a -> a.getDoctor().getSpecialization())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (specialization != null && !specialization.isEmpty()) {
            myAppointments = myAppointments.stream()
                    .filter(app -> specialization.equals(app.getDoctor().getSpecialization()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("appointments", myAppointments);
        model.addAttribute("specializations", specializations);
        model.addAttribute("selectedSpecialization", specialization);

        return "patient/my_appointments";
    }

    // отмена записи
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

    // просмотр собственного профиля
    @GetMapping("/profile")
    public String viewProfile(Model model) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);

        if (user != null) {
            List<Appointment> appointments = appointmentRepository.findByUserOrderByDateTimeDesc(user);
            model.addAttribute("user", user);
            model.addAttribute("appointments", appointments);
        }

        return "patient/profile";
    }
}

