package com.example.clinic.repository;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUser(User patient);
//    List<Appointment> findByDoctor(Doctor doctor);
}
