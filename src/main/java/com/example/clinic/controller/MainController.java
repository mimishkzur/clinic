// основной контроллер

package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.repository.DoctorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class MainController {

    private final DoctorRepository doctorRepository;

    public MainController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // главная страница
    @GetMapping("/")
    public String home(Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);
        return "home";
    }

    // информация о клинике
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    // информация об авторе
    @GetMapping("/author")
    public String author() {
        return "author";
    }

    // информация о враче
    @GetMapping("/public-doctor/{email}")
    public String viewDoctor(@PathVariable String email, Model model) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Врач не найден"));
        model.addAttribute("doctor", doctor);
        return "doctor/public_profile";
    }
}
