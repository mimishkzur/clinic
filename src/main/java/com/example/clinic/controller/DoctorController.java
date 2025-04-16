package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "doctor/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Doctor doctor = doctorRepository.findByEmail(userDetails.getUsername()).orElse(new Doctor());
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute Doctor formDoctor,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Doctor doctor = doctorRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (doctor != null) {
            doctor.setBirthDate(formDoctor.getBirthDate());
            doctor.setEducation(formDoctor.getEducation());
            doctor.setExperience(formDoctor.getExperience());
            doctor.setDescription(formDoctor.getDescription());
            doctorRepository.save(doctor);
        }
        return "redirect:/doctor/profile";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, Principal principal) {
        String email = principal.getName();
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        List<Appointment> allAppointments = appointmentRepository.findByDoctor(doctor);
        LocalDateTime now = LocalDateTime.now();

        List<Appointment> past = new ArrayList<>();
        List<Appointment> today = new ArrayList<>();
        List<Appointment> future = new ArrayList<>();

        for (Appointment appointment : allAppointments) {
            LocalDateTime dateTime = appointment.getDateTime();
            if (dateTime.toLocalDate().isBefore(now.toLocalDate())) {
                past.add(appointment);
            } else if (dateTime.toLocalDate().isEqual(now.toLocalDate())) {
                today.add(appointment);
            } else {
                future.add(appointment);
            }
        }

        model.addAttribute("pastAppointments", past);
        model.addAttribute("todayAppointments", today);
        model.addAttribute("futureAppointments", future);
        return "doctor/appointments";
    }

    @GetMapping("/appointments/{id}/edit")
    public String editAppointment(@PathVariable Long id, Model model, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приём не найден"));

        // Проверка: врач имеет доступ только к своим приёмам
        Doctor doctor = doctorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        if (!appointment.getDoctor().getEmail().equals(doctor.getEmail())) {
            throw new RuntimeException("Доступ запрещён");
        }

        model.addAttribute("appointment", appointment);
        return "doctor/appointment_form";
    }

    @PostMapping("/appointments/{id}")
    public String updateAppointment(@PathVariable Long id,
                                    @ModelAttribute Appointment formAppointment,
                                    Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приём не найден"));

        // Проверка, что врач сохраняет только свои приёмы
        Doctor doctor = doctorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        if (!appointment.getDoctor().getEmail().equals(doctor.getEmail())) {
            throw new RuntimeException("Доступ запрещён");
        }

        appointment.setDiagnosis(formAppointment.getDiagnosis());
        appointment.setTreatment(formAppointment.getTreatment());
        appointment.setNotes(formAppointment.getNotes());
        appointmentRepository.save(appointment);

        return "redirect:/doctor/appointments";
    }


}
