package com.medicare.backend.controller;

import com.medicare.backend.model.Appointment;
import com.medicare.backend.model.Doctor;
import com.medicare.backend.model.Patient;
import com.medicare.backend.model.Role;
import com.medicare.backend.model.User;
import com.medicare.backend.repository.AppointmentRepository;
import com.medicare.backend.repository.DoctorRepository;
import com.medicare.backend.repository.PatientRepository;
import com.medicare.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository,
                             PatientRepository patientRepository,
                             DoctorRepository doctorRepository,
                             AppointmentRepository appointmentRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get current logged-in user's profile (Patient or Doctor).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.PATIENT) {
            return patientRepository.findByUser(user)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(Map.of("user", user)));
        } else if (user.getRole() == Role.DOCTOR) {
            return doctorRepository.findByUser(user)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.ok(Map.of("user", user)));
        }

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    /**
     * Delete current logged-in user's profile and account.
     * Accessible by PATIENT, DOCTOR, or ADMIN from their own page.
     */
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> deleteMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identifier = auth.getName();

        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier));

        if (user.getRole() == Role.PATIENT) {
            Optional<Patient> patientOpt = patientRepository.findByUser(user);
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
                appointmentRepository.deleteAll(appointments);
                patientRepository.delete(patient);
            }
        } else if (user.getRole() == Role.DOCTOR) {
            Optional<Doctor> doctorOpt = doctorRepository.findByUser(user);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
                appointmentRepository.deleteAll(appointments);
                doctorRepository.delete(doctor);
            }
        }

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Account and profile deleted successfully"));
    }

    /**
     * Update patient's own profile (email, phone, address, blood group, allergies).
     */
    @PutMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    @Transactional
    public ResponseEntity<?> updatePatientProfile(@RequestBody Map<String, String> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .or(() -> userRepository.findByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUser(user)
                .orElseGet(() -> {
                    Patient newP = new Patient(user, user.getName());
                    newP.setEmail(user.getEmail());
                    return newP;
                });

        if (updates.containsKey("name") && updates.get("name") != null && !updates.get("name").isBlank()) {
            patient.setName(updates.get("name"));
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email") && updates.get("email") != null && !updates.get("email").isBlank()) {
            patient.setEmail(updates.get("email"));
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            patient.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("address")) {
            patient.setAddress(updates.get("address"));
        }
        if (updates.containsKey("bloodGroup")) {
            patient.setBloodGroup(updates.get("bloodGroup"));
        }
        if (updates.containsKey("allergies")) {
            patient.setAllergies(updates.get("allergies"));
        }
        if (updates.containsKey("gender")) {
            patient.setGender(updates.get("gender"));
        }
        if (updates.containsKey("dob") && updates.get("dob") != null && !updates.get("dob").isBlank()) {
            try {
                java.time.LocalDate birthDate = java.time.LocalDate.parse(updates.get("dob").trim());
                patient.setAge(java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears());
            } catch (Exception ignored) {}
        } else if (updates.containsKey("age") && updates.get("age") != null && !updates.get("age").isBlank()) {
            try { patient.setAge(Integer.parseInt(updates.get("age").trim())); } catch (Exception ignored) {}
        }

        // Optionally update password
        if (updates.containsKey("password") && !updates.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        Patient saved = patientRepository.save(patient);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update doctor's own profile (email, phone, working hours, available days, consultation fee).
     */
    @PutMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    @Transactional
    public ResponseEntity<?> updateDoctorProfile(@RequestBody Map<String, String> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .or(() -> userRepository.findByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorRepository.findByUser(user)
                .orElseGet(() -> {
                    Doctor newDoc = new Doctor(user, user.getName(), "General Medicine");
                    newDoc.setEmail(user.getEmail());
                    return newDoc;
                });

        if (updates.containsKey("name") && updates.get("name") != null && !updates.get("name").isBlank()) {
            doctor.setName(updates.get("name"));
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email") && updates.get("email") != null && !updates.get("email").isBlank()) {
            doctor.setEmail(updates.get("email"));
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            doctor.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("specialization")) {
            doctor.setSpecialization(updates.get("specialization"));
        }
        if (updates.containsKey("experience")) {
            doctor.setExperience(updates.get("experience"));
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

        // Optionally update password
        if (updates.containsKey("password") && !updates.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        Doctor savedDoc = doctorRepository.save(doctor);
        return ResponseEntity.ok(savedDoc);
    }

    /**
     * Admin updates their own details (name, email, password).
     */
    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateAdminProfile(@RequestBody Map<String, String> updates) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .or(() -> userRepository.findByEmail(auth.getName()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("password") && !updates.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "message", "Admin profile updated successfully"
        ));
    }
}
