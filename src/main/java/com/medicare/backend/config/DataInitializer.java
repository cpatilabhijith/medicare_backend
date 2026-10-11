package com.medicare.backend.config;

import com.medicare.backend.model.Doctor;
import com.medicare.backend.model.Patient;
import com.medicare.backend.model.Role;
import com.medicare.backend.model.User;
import com.medicare.backend.repository.DoctorRepository;
import com.medicare.backend.repository.PatientRepository;
import com.medicare.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedDoctors();
        seedPatients();
    }

    private void seedDoctors() {
        record DoctorSeed(String username, String name, String email, String specialization,
                          String experience, String phone, String availableDays,
                          String workingHours, String consultationFee) {}

        List<DoctorSeed> seeds = List.of(
                new DoctorSeed("drjenkins", "Dr. Sarah Jenkins", "s.jenkins@medicare.com", "Cardiology",
                        "12 years", "+1 (555) 234-5678", "Monday, Wednesday, Friday",
                        "09:00 AM - 04:00 PM", "$120"),
                new DoctorSeed("drmarcus", "Dr. Marcus Vance", "m.vance@medicare.com", "Neurology",
                        "15 years", "+1 (555) 345-6789", "Tuesday, Thursday",
                        "10:00 AM - 05:00 PM", "$150"),
                new DoctorSeed("dremily", "Dr. Emily Chen", "e.chen@medicare.com", "Pediatrics",
                        "8 years", "+1 (555) 456-7890", "Monday, Tuesday, Wednesday, Friday",
                        "08:30 AM - 03:30 PM", "$90"),
                new DoctorSeed("drrodriguez", "Dr. Robert Rodriguez", "r.rodriguez@medicare.com", "Orthopedics",
                        "14 years", "+1 (555) 567-8901", "Monday, Wednesday, Thursday",
                        "09:00 AM - 05:00 PM", "$130"),
                new DoctorSeed("drsophia", "Dr. Sophia Reynolds", "s.reynolds@medicare.com", "General Medicine",
                        "10 years", "+1 (555) 678-9012", "Monday, Tuesday, Thursday, Friday",
                        "08:00 AM - 04:00 PM", "$100"),
                new DoctorSeed("drkim", "Dr. David Kim", "d.kim@medicare.com", "Oncology",
                        "16 years", "+1 (555) 789-0123", "Tuesday, Wednesday, Friday",
                        "09:30 AM - 04:30 PM", "$160")
        );

        for (DoctorSeed seed : seeds) {
            if (!userRepository.existsByEmail(seed.email()) && !userRepository.existsByUsername(seed.username())) {
                User user = new User(
                        seed.username(),
                        passwordEncoder.encode("Password@123"),
                        seed.email(),
                        seed.name(),
                        Role.DOCTOR
                );
                user = userRepository.save(user);

                Doctor doctor = new Doctor(user, seed.name(), seed.specialization());
                doctor.setExperience(seed.experience());
                doctor.setPhone(seed.phone());
                doctor.setEmail(seed.email());
                doctor.setAvailableDays(seed.availableDays());
                doctor.setWorkingHours(seed.workingHours());
                doctor.setConsultationFee(seed.consultationFee());
                doctorRepository.save(doctor);
            }
        }
    }

    private void seedPatients() {
        record PatientSeed(String username, String name, String email, String phone,
                           int age, String gender, String bloodGroup, String allergies, String address) {}

        List<PatientSeed> seeds = List.of(
                new PatientSeed("alexmorgan", "Alex Morgan", "alex.morgan@email.com", "+1 (555) 789-0123",
                        32, "FEMALE", "O+", "Penicillin", "742 Evergreen Terr, Springfield"),
                new PatientSeed("jameswilson", "James Wilson", "j.wilson@email.com", "+1 (555) 890-1234",
                        45, "MALE", "A+", "None", "123 Baker Street, London"),
                new PatientSeed("sophiam", "Sophia Martinez", "sophia.m@email.com", "+1 (555) 901-2345",
                        27, "FEMALE", "B+", "Aspirin", "456 Elm Street, Metropolis"),
                new PatientSeed("liama", "Liam Anderson", "liam.anderson@email.com", "+1 (555) 123-4567",
                        38, "MALE", "AB+", "Sulfa drugs", "88 Pine Avenue, Seattle"),
                new PatientSeed("emmaw", "Emma Watson", "emma.watson@email.com", "+1 (555) 234-8901",
                        29, "FEMALE", "O-", "Pollen, Peanuts", "502 Oak Lane, Boston"),
                new PatientSeed("rajeshk", "Rajesh Kumar", "rajesh.kumar@email.com", "+1 (555) 345-0192",
                        52, "MALE", "B-", "None", "14 Lake View Road, Chicago")
        );

        for (PatientSeed seed : seeds) {
            if (!userRepository.existsByEmail(seed.email()) && !userRepository.existsByUsername(seed.username())) {
                User user = new User(
                        seed.username(),
                        passwordEncoder.encode("Password@123"),
                        seed.email(),
                        seed.name(),
                        Role.PATIENT
                );
                user = userRepository.save(user);

                Patient patient = new Patient(user, seed.name());
                patient.setPhone(seed.phone());
                patient.setEmail(seed.email());
                patient.setAge(seed.age());
                patient.setGender(seed.gender());
                patient.setBloodGroup(seed.bloodGroup());
                patient.setAllergies(seed.allergies());
                patient.setAddress(seed.address());
                patientRepository.save(patient);
            }
        }
    }
}
