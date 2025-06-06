package com.example.clinic.repository;

import com.example.clinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);
    Optional<Doctor> findByEmail(String email);

    @Modifying
    @Query("DELETE FROM Doctor d WHERE d.email = :email")
    void deleteByEmail(@Param("email") String email);
}
