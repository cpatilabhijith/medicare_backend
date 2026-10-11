package com.medicare.backend.controller;

import com.medicare.backend.model.Doctor;
import com.medicare.backend.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private com.medicare.backend.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.medicare.backend.repository.UserRepository userRepository;

    // Public: list all doctors for patients to browse
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
        return ResponseEntity.ok(doctor);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorRepository.save(doctor));
    }

    // Doctor or Admin can update schedule & details
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        if (updates.containsKey("name")) {
            doctor.setName(updates.get("name"));
            if (doctor.getUser() != null) {
                doctor.getUser().setName(updates.get("name"));
                userRepository.save(doctor.getUser());
            }
        }
        if (updates.containsKey("specialization")) {
            doctor.setSpecialization(updates.get("specialization"));
        }
        if (updates.containsKey("experience")) {
            doctor.setExperience(updates.get("experience"));
        }
        if (updates.containsKey("phone")) {
            doctor.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            doctor.setEmail(updates.get("email"));
            if (doctor.getUser() != null) {
                doctor.getUser().setEmail(updates.get("email"));
                userRepository.save(doctor.getUser());
            }
        }
        if (updates.containsKey("workingHours")) {
            doctor.setWorkingHours(updates.get("workingHours"));
        }
        if (updates.containsKey("availableDays")) {
            doctor.setAvailableDays(updates.get("availableDays"));
        }
        if (updates.containsKey("consultationFee")) {
            doctor.setConsultationFee(updates.get("consultationFee"));
        }

        return ResponseEntity.ok(doctorRepository.save(doctor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        // Delete associated appointments
        List<com.medicare.backend.model.Appointment> appointments = appointmentRepository.findByDoctorId(id);
        appointmentRepository.deleteAll(appointments);

        com.medicare.backend.model.User user = doctor.getUser();
        doctorRepository.delete(doctor);

        if (user != null) {
            userRepository.delete(user);
        }

        return ResponseEntity.ok(Map.of("message", "Doctor and associated account deleted successfully"));
    }
}
