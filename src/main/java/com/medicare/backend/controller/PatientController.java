package com.medicare.backend.controller;

import com.medicare.backend.model.Patient;
import com.medicare.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private com.medicare.backend.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.medicare.backend.repository.UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Patient> createPatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientRepository.save(patient));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient updates) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        if (updates.getName() != null) {
            patient.setName(updates.getName());
            if (patient.getUser() != null) {
                patient.getUser().setName(updates.getName());
                userRepository.save(patient.getUser());
            }
        }
        if (updates.getEmail() != null) {
            patient.setEmail(updates.getEmail());
            if (patient.getUser() != null) {
                patient.getUser().setEmail(updates.getEmail());
                userRepository.save(patient.getUser());
            }
        }
        if (updates.getPhone() != null) patient.setPhone(updates.getPhone());
        if (updates.getAddress() != null) patient.setAddress(updates.getAddress());
        if (updates.getAllergies() != null) patient.setAllergies(updates.getAllergies());
        if (updates.getBloodGroup() != null) patient.setBloodGroup(updates.getBloodGroup());
        if (updates.getAge() != null) patient.setAge(updates.getAge());
        if (updates.getGender() != null) patient.setGender(updates.getGender());

        return ResponseEntity.ok(patientRepository.save(patient));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        // Delete associated appointments
        List<com.medicare.backend.model.Appointment> appointments = appointmentRepository.findByPatientId(id);
        appointmentRepository.deleteAll(appointments);

        com.medicare.backend.model.User user = patient.getUser();
        patientRepository.delete(patient);

        if (user != null) {
            userRepository.delete(user);
        }

        return ResponseEntity.ok(java.util.Map.of("message", "Patient and associated account deleted successfully"));
    }
}
