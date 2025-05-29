package com.example.clinic.repository;

import com.example.clinic.model.Appointment;
import com.example.clinic.model.Doctor;
import com.example.clinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByUserIsNullOrderByDateTimeAsc();
    List<Appointment> findByUserOrderByDateTimeDesc(User patient);

    @Modifying
    @Query("UPDATE Appointment a SET a.user = null WHERE a.user.email = :email")
    void detachAppointmentsFromUser(@Param("email") String email);

    // сегодняшние приемы - возрастание
    List<Appointment> findByDoctorAndDateTimeBetweenOrderByDateTimeAsc(
            Doctor doctor,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay);

    // будущие приемы - возрастание
    List<Appointment> findByDoctorAndDateTimeAfterOrderByDateTimeAsc(
            Doctor doctor,
            LocalDateTime dateTime);

    // прошлые приемы - убывание
    List<Appointment> findByDoctorAndDateTimeBeforeOrderByDateTimeDesc(
            Doctor doctor,
            LocalDateTime dateTime);

    boolean existsByDoctorAndDateTime(Doctor doctor, LocalDateTime dateTime);
}
