package com.medicare.backend.controller;

import com.medicare.backend.dto.*;
import com.medicare.backend.model.*;
import com.medicare.backend.repository.*;
import com.medicare.backend.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getUsername() != null ? loginRequest.getUsername().trim() : "";
        String lowerIdentifier = identifier.toLowerCase();
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByUsername(lowerIdentifier))
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByEmail(lowerIdentifier))
                .orElseThrow(() -> new RuntimeException("Invalid credentials: User not found"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        return ResponseEntity.ok(new JwtResponse(
                jwt, user.getId(), user.getUsername(),
                user.getEmail(), user.getName(), user.getRole().name()
        ));
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }

        String username = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : "";
        if (username.isBlank()) {
            username = email.split("@")[0].replaceAll("[^a-z0-9]", "");
        }
        if (username.length() < 3) {
            username = username + "user";
        }
        // If username exists but email is unique, auto-suffix to avoid registration failure
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + (System.currentTimeMillis() % 10000);
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = new User(
                username,
                passwordEncoder.encode(request.getPassword()),
                email,
                request.getName(),
                role
        );
        userRepository.save(user);

        // Create linked profile based on role
        if (role == Role.DOCTOR) {
            String specialization = request.getSpecialization() != null && !request.getSpecialization().isBlank()
                    ? request.getSpecialization() : "General Medicine";
            Doctor doctor = new Doctor(user, request.getName(), specialization);
            doctor.setExperience(request.getExperience() != null ? request.getExperience() : "5+ years");
            doctor.setPhone(request.getPhone() != null ? request.getPhone() : "+1 (555) 000-0000");
            doctor.setEmail(email);
            doctor.setAvailableDays(request.getAvailableDays() != null ? request.getAvailableDays() : "Monday,Wednesday,Friday");
            doctor.setWorkingHours(request.getWorkingHours() != null ? request.getWorkingHours() : "09:00 AM - 05:00 PM");
            doctor.setConsultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : "$100");
            doctorRepository.save(doctor);
        } else if (role == Role.PATIENT) {
            Patient patient = new Patient(user, request.getName());
            patient.setPhone(request.getPhone());
            patient.setEmail(email);
            patient.setAge(request.calculateAge());
            patient.setGender(request.getGender() != null ? request.getGender() : "MALE");
            patient.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup() : "O+");
            patient.setAllergies(request.getAllergies() != null ? request.getAllergies() : "None");
            patient.setAddress(request.getAddress() != null ? request.getAddress() : "Medicare Community Care");
            patientRepository.save(patient);
        }

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully!",
                "username", username,
                "email", email
        ));
    }
}
