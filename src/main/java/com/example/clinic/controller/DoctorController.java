// функционал врача

package com.example.clinic.controller;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.AppointmentStatus;
import com.example.clinic.model.Doctor;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    // загрузка пути для сохранения файлов
    @Value("${upload.path}")
    private String uploadPath;

    // главная страница врача
    @GetMapping("/dashboard")
    public String dashboard() {
        return "doctor/dashboard";
    }

    // профиль врача
    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Doctor doctor = doctorRepository.findByEmail(userDetails.getUsername()).orElse(new Doctor());
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    // обновление информации в профиле врача
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute Doctor formDoctor,
                                @RequestParam("photo") MultipartFile photo,
                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Doctor doctor = doctorRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (doctor != null) {
            doctor.setBirthDate(formDoctor.getBirthDate());
            doctor.setEducation(formDoctor.getEducation());
            doctor.setWorkStartDate(formDoctor.getWorkStartDate());
            doctor.setDescription(formDoctor.getDescription());

            if (!photo.isEmpty()) {
                Path uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String uuidFile = UUID.randomUUID().toString();
                String fileExtension = photo.getOriginalFilename()
                        .substring(photo.getOriginalFilename().lastIndexOf("."));
                String resultFilename = uuidFile + fileExtension;

                Path filePath = uploadDir.resolve(resultFilename);
                photo.transferTo(filePath.toFile());

                doctor.setPhotoPath(resultFilename);
            }

            doctorRepository.save(doctor);
        }
        return "redirect:/doctor/profile";
    }

    // просмотр записей
    @GetMapping("/appointments")
    public String viewAppointments(Model model, Principal principal) {
        String email = principal.getName();
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = now.toLocalDate().atTime(23, 59, 59);

        // Сегодняшние приемы - сортируем по времени (возрастание)
        List<Appointment> today = appointmentRepository
                .findByDoctorAndDateTimeBetweenOrderByDateTimeAsc(
                        doctor,
                        startOfToday,
                        endOfToday);

        // Будущие приемы - сортируем по дате (возрастание)
        List<Appointment> future = appointmentRepository
                .findByDoctorAndDateTimeAfterOrderByDateTimeAsc(
                        doctor,
                        endOfToday);

        // Прошедшие приемы - сортируем по дате (убывание)
        List<Appointment> past = appointmentRepository
                .findByDoctorAndDateTimeBeforeOrderByDateTimeDesc(
                        doctor,
                        startOfToday);

        model.addAttribute("pastAppointments", past);
        model.addAttribute("todayAppointments", today);
        model.addAttribute("futureAppointments", future);
        return "doctor/appointments";
    }

    // редактирование записи
    @GetMapping("/appointments/{id}/edit")
    public String editAppointment(@PathVariable Long id, Model model, Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приём не найден"));

        Doctor doctor = doctorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        if (!appointment.getDoctor().getEmail().equals(doctor.getEmail())) {
            throw new RuntimeException("Доступ запрещён");
        }

        model.addAttribute("appointment", appointment);
        return "doctor/appointment_form";
    }

    // сохранение изменений редактирования записи
    @PostMapping("/appointments/{id}")
    public String updateAppointment(@PathVariable Long id,
                                    @ModelAttribute Appointment formAppointment,
                                    Principal principal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Приём не найден"));

        Doctor doctor = doctorRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));

        if (!appointment.getDoctor().getEmail().equals(doctor.getEmail())) {
            throw new RuntimeException("Доступ запрещён");
        }

        appointment.setDiagnosis(formAppointment.getDiagnosis());
        appointment.setTreatment(formAppointment.getTreatment());
        appointment.setNotes(formAppointment.getNotes());
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return "redirect:/doctor/appointments";
    }

}
