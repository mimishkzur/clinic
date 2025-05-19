// работа с врачами - не используется

package com.example.clinic.service;

import com.example.clinic.model.Doctor;
import com.example.clinic.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // список врачей
    public List<Doctor> listDoctors(String specialization) {
        if (specialization != null) return doctorRepository.findBySpecialization(specialization);
        return doctorRepository.findAll();
    }

    // сохранение врача
    public void saveDoctor(Doctor doctor) {
        doctorRepository.save(doctor);
    }

    // удаление врача
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    // получение врача по id
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }
}
