package com.example.clinic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

//    @ManyToOne
//    private Doctor doctor;

    private LocalDateTime dateTime;

    private String diagnosis;
    private String Treatment;
    private String notes;

//    @Enumerated(EnumType.STRING)
//    private AppointmentStatus status;
}
