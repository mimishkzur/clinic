// регистрация и аутентификация пользователей

package com.example.clinic.controller;

import com.example.clinic.model.Role;
import com.example.clinic.model.User;
import com.example.clinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // отображение формы регистрации
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // обработка регистрации
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {

        // проверка email
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            model.addAttribute("emailError", "Email не может быть пустым");
            return "auth/register";
        }
        if (!user.getEmail().contains("@")) {
            model.addAttribute("emailError", "Email должен содержать @");
            return "auth/register";
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("emailError", "Пользователь с таким email уже существует");
            return "auth/register";
        }

        // проверка ФИО
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            model.addAttribute("fullNameError", "ФИО не может быть пустым");
            return "auth/register";
        }
        String[] nameParts = user.getFullName().split("\\s+");
        if (nameParts.length != 3) {
            model.addAttribute("fullNameError", "ФИО должно состоять из трех слов");
            return "auth/register";
        }

        // проверка телефона (новое условие)
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            model.addAttribute("phoneError", "Телефон не может быть пустым");
            return "auth/register";
        }
        // удаляем все нецифровые символы и проверяем длину
        String cleanPhone = user.getPhone().replaceAll("[^0-9]", "");
        if (cleanPhone.length() != 11) {
            model.addAttribute("phoneError", "Номер телефона должен содержать 11 цифр");
            return "auth/register";
        }
        // сохраняем очищенный номер
        user.setPhone(cleanPhone);

        // проверка номера полиса
        if (user.getOmsPolicyNumber() == null || user.getOmsPolicyNumber().isEmpty()) {
            model.addAttribute("omsError", "Номер полиса не может быть пустым");
            return "auth/register";
        }
        if (!user.getOmsPolicyNumber().matches("\\d{16}")) {
            model.addAttribute("omsError", "Номер полиса должен состоять из 16 цифр");
            return "auth/register";
        }

        // проверка пароля
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            model.addAttribute("passwordError", "Пароль не может быть пустым");
            return "auth/register";
        }
        if (user.getPassword().length() < 5 || user.getPassword().length() > 15) {
            model.addAttribute("passwordError", "Пароль должен быть от 5 до 15 символов");
            return "auth/register";
        }

        // если все проверки пройдены
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(Role.PATIENT);
            userRepository.save(user);
            System.out.println("Пользователь успешно сохранен: " + user.getEmail());
            return "redirect:/login";
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении пользователя: " + e.getMessage());
            model.addAttribute("error", "Произошла ошибка при регистрации");
            return "auth/register";
        }
    }

    // отображение формы входа
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    // обработка выхода из аккаунта
    @GetMapping("/logout")
    public String showHomePage() {
        return "home";
    }
}
