package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    private final DoctorRepository doctorRepository;

    public MainController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        model.addAttribute("doctors", doctors);
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }


}
