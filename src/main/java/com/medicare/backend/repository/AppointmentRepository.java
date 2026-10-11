package com.medicare.backend.repository;

import com.medicare.backend.model.Appointment;
import com.medicare.backend.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    // Used for duplicate slot validation
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
        Long doctorId, LocalDate appointmentDate, String appointmentTime, AppointmentStatus status
    );
}
