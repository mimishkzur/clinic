package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.AppointmentStatus;
import com.example.clinic.model.User;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

//    @GetMapping("/appointments/available")
//    public String viewAvailableAppointments(Model model) {
//        List<Appointment> availableAppointments = appointmentRepository.findByUserIsNullOrderByDateTimeAsc();
//        model.addAttribute("appointments", availableAppointments);
//        return "patient/available_appointments";
//    }
    @GetMapping("/appointments/available")
    public String viewAvailableAppointments(@RequestParam(required = false) String specialization, Model model) {
        List<Appointment> availableAppointments = appointmentRepository.findByUserIsNullOrderByDateTimeAsc();

        // Получаем список специализаций всех врачей
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

//    @GetMapping("/my-appointments")
//    public String myAppointments(Model model, Principal principal) {
//        String email = principal.getName();
//        User user = userRepository.findByEmail(email).orElseThrow();
//        List<Appointment> myAppointments = appointmentRepository.findByUser(user);
//        model.addAttribute("appointments", myAppointments);
//        return "patient/my_appointments";
//    }
    @GetMapping("/my-appointments")
    public String myAppointments(@RequestParam(required = false) String specialization, Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Appointment> myAppointments = appointmentRepository.findByUser(user);

        // Получаем список специализаций врача из моих приёмов
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

