// администрирование клиники

package com.example.clinic.controller;

import com.example.clinic.model.*;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.TreeMap;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    // просмотр списка пользователей
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

    // изменение роли пользователя
    @PostMapping("/users/{email}/role")
    public String changeUserRole(@PathVariable String email, @RequestParam Role role) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);

            if (role == Role.DOCTOR) {
                doctorRepository.findByEmail(email).orElseGet(() -> {
                    Doctor doctor = new Doctor();
                    doctor.setEmail(user.getEmail());
                    doctor.setFullName(user.getFullName());
                    doctor.setPhone(user.getPhone());
                    doctor.setSpecialization("Не указана");
                    doctor.setSalary(0);
                    doctorRepository.save(doctor);
                    return doctor;
                });
            }
        });
        return "redirect:/admin/users";
    }

    // просмотр и фильтрация записей
    @GetMapping("/appointments")
    public String showAppointmentsForm(@RequestParam(required = false) String doctorEmail, Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);
        model.addAttribute("selectedDoctorEmail", doctorEmail);
        model.addAttribute("appointment", new Appointment());

        if (doctorEmail != null && !doctorEmail.isEmpty()) {
            doctorRepository.findByEmail(doctorEmail).ifPresent(doctor -> {
                List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);

                appointments.sort((a1, a2) -> a2.getDateTime().compareTo(a1.getDateTime()));

                model.addAttribute("appointments", appointments);
            });
        }

        return "admin/appointments";
    }

    // добавление новой записи
    @PostMapping("/appointments")
    public String addAppointment(@RequestParam(name = "dateTime", required = false) String dateTimeStr,
                                 @RequestParam String doctorEmail,
                                 Model model) {

        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("selectedDoctorEmail", doctorEmail);

        if (doctorEmail != null && !doctorEmail.isEmpty()) {
            doctorRepository.findByEmail(doctorEmail).ifPresent(doctor -> {
                List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);
                appointments.sort((a1, a2) -> a2.getDateTime().compareTo(a1.getDateTime()));
                model.addAttribute("appointments", appointments);
            });
        }

        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            model.addAttribute("dateTimeError", "Дата и время обязательны для заполнения");
            return "admin/appointments";
        }

        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime maxAllowedDate = now.plusMonths(1);

            if (dateTime.isBefore(LocalDateTime.now())) {
                model.addAttribute("dateTimeError", "Дата и время не могут быть в прошлом");
                return "admin/appointments";
            }

            if (dateTime.isAfter(maxAllowedDate)) {
                model.addAttribute("dateTimeError", "Дата приема не может быть больше, чем на 1 месяц от текущей даты");
                return "admin/appointments";
            }

            Optional<Doctor> doctorOpt = doctorRepository.findByEmail(doctorEmail);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                if (appointmentRepository.existsByDoctorAndDateTime(doctor, dateTime)) {
                    model.addAttribute("dateTimeError", "У врача уже есть приём в это время");
                    return "admin/appointments";
                }

                Appointment appointment = new Appointment();
                appointment.setDateTime(dateTime);
                appointment.setDoctor(doctor);
                appointment.setStatus(AppointmentStatus.AVAILABLE);
                appointmentRepository.save(appointment);
            }

        } catch (DateTimeParseException e) {
            model.addAttribute("dateTimeError", "Некорректный формат даты и времени");
            return "admin/appointments";
        }

        return "redirect:/admin/appointments?doctorEmail=" + doctorEmail;
    }

    // редактирование информации о враче (форма редактирования)
    @GetMapping("/doctors/{email}/edit")
    public String editDoctor(@PathVariable String email, Model model) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Доктор не найден"));
        model.addAttribute("doctor", doctor);
        return "admin/edit_doctor";
    }

    // редактирование информации о враче (сохранение изменений)
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

    // статистика
    @GetMapping("/statistics")
    public String statistics(Model model) {
        List<Appointment> appointments = appointmentRepository.findAll();

        // группировка по датам
        Map<LocalDate, Long> appointmentsPerDay = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDateTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        // группировка по врачам
        Map<String, Long> appointmentsPerDoctor = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getFullName(),
                        Collectors.counting()
                ));

        // группировка по статусам приемов
        Map<String, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().toString(),
                        Collectors.counting()
                ));

        model.addAttribute("appointmentsPerDay", appointmentsPerDay);
        model.addAttribute("appointmentsPerDoctor", appointmentsPerDoctor);
        model.addAttribute("statusCounts", statusCounts);

        return "admin/statistics";
    }

    // удаление пользователя
    @PostMapping("/users/{email}/delete")
    @Transactional
    public String deleteUser(@PathVariable String email) {
        if ("admin@ya.ru".equals(email)) {
            return "redirect:/admin/users?error=Cannot delete main admin";
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            appointmentRepository.detachAppointmentsFromUser(email);

            if (user.getRole() == Role.DOCTOR) {
                doctorRepository.deleteByEmail(email);
            }

            userRepository.delete(user);
        });

        return "redirect:/admin/users?success=User deleted successfully";
    }
}
