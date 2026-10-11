package com.medicare.backend.service;

import com.medicare.backend.dto.BookAppointmentRequest;
import com.medicare.backend.model.Appointment;
import com.medicare.backend.model.AppointmentStatus;
import com.medicare.backend.model.Doctor;
import com.medicare.backend.model.Patient;
import com.medicare.backend.repository.AppointmentRepository;
import com.medicare.backend.repository.DoctorRepository;
import com.medicare.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Book an appointment with slot overlap validation.
     * Prevents duplicate bookings for the same doctor, date, and time.
     */
    @Transactional
    public Appointment bookAppointment(Long patientId, BookAppointmentRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .or(() -> patientRepository.findByUserId(patientId))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with id: " + patientId));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .or(() -> doctorRepository.findByUserId(request.getDoctorId()))
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with id: " + request.getDoctorId()));

        LocalDate date = LocalDate.parse(request.getAppointmentDate().trim());

        // Validate that appointment date is not in the past
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book an appointment for a past date.");
        }

        String time = request.getAppointmentTime().trim();

        // *** SLOT VALIDATION: Prevent duplicate bookings ***
        boolean slotTaken = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        doctor.getId(), date, time, AppointmentStatus.CANCELLED
                );

        if (slotTaken) {
            throw new IllegalStateException("This time slot is already booked for the selected doctor and date.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setReason(request.getReason() != null ? request.getReason().trim() : "");
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        Long resolvedPatientId = patientRepository.findById(patientId)
                .or(() -> patientRepository.findByUserId(patientId))
                .map(patient -> patient.getId())
                .orElse(patientId);
        return appointmentRepository.findByPatientId(resolvedPatientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        Long resolvedDoctorId = doctorRepository.findById(doctorId)
                .or(() -> doctorRepository.findByUserId(doctorId))
                .map(doctor -> doctor.getId())
                .orElse(doctorId);
        return appointmentRepository.findByDoctorId(resolvedDoctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public Appointment updateStatus(Long appointmentId, String status, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));

        if (status != null && !status.isBlank()) {
            appointment.setStatus(AppointmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)));
        }
        if (notes != null && !notes.isBlank()) {
            appointment.setNotes(notes.trim());
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + appointmentId));
        appointmentRepository.delete(appointment);
    }
}
