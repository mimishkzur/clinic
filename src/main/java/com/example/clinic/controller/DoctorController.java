//package com.example.clinic.controller;
//
//import com.example.clinic.model.Doctor;
//import com.example.clinic.repository.DoctorRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//
//@Controller
//@RequestMapping("/doctors")
//public class DoctorController {
//
//    private final DoctorRepository doctorRepository;
//
//    @Autowired
//    public DoctorController(DoctorRepository doctorRepository) {
//        this.doctorRepository = doctorRepository;
//    }
//
//    @GetMapping
//    public String listDoctors(Model model) {
//        model.addAttribute("doctors", doctorRepository.findAll());
//        model.addAttribute("newDoctor", new Doctor());
//        return "doctors/list";
//    }
//
//    @GetMapping("/new")
//    public String showAddForm(Model model) {
//        model.addAttribute("doctor", new Doctor());
//        return "doctors/add-form";
//    }
//
//    @PostMapping("/add")
//    public String addDoctor(@ModelAttribute Doctor new_doctor) {
//        doctorRepository.save(new_doctor);
//        return "redirect:/doctors";
//    }

//    @GetMapping("/profile")
//    public String showDoctorProfileForm(Model model, Principal principal) {
//        String email = principal.getName();
//        Doctor doctor = doctorRepository.findByEmail(email).orElse(new Doctor());
//        doctor.setEmail(email); // если новый
//        model.addAttribute("doctor", doctor);
//        return "doctor/profile-form";
//    }

//    @PostMapping("/profile")
//    public String saveDoctorProfile(@ModelAttribute Doctor doctor, Principal principal) {
//        doctor.setEmail(principal.getName()); // гарантируем, что сохраняется текущий email
//        doctorRepository.save(doctor);
//        return "redirect:/"; // или на дашборд
//    }
//}

package com.example.clinic.controller;

import com.example.clinic.model.Doctor;
import com.example.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;

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
}
