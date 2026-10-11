package com.medicare.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 120)
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String role; // "PATIENT", "DOCTOR", or "ADMIN"

    // Doctor-specific fields (optional, only when role=DOCTOR)
    private String specialization;
    private String experience;
    private String phone;
    private String availableDays;
    private String workingHours;
    private String consultationFee;

    // Patient-specific fields (optional, only when role=PATIENT)
    private String dob;     // Date of Birth in "YYYY-MM-DD" format — age calculated from this
    private Integer age;    // Fallback if dob not provided
    private String gender;
    private String bloodGroup;
    private String allergies;
    private String address;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getConsultationFee() { return consultationFee; }
    public void setConsultationFee(String consultationFee) { this.consultationFee = consultationFee; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    /**
     * Calculate age from DOB string (YYYY-MM-DD).
     * Returns the manually set age if DOB is not provided.
     */
    public Integer calculateAge() {
        if (dob != null && !dob.isBlank()) {
            try {
                java.time.LocalDate birthDate = java.time.LocalDate.parse(dob.trim());
                return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
            } catch (Exception ignored) {}
        }
        return age; // fallback
    }
}
