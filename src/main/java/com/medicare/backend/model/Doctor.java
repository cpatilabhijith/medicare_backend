package com.medicare.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"password"})
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    private String experience;

    private String phone;

    private String email;

    @Column(name = "available_days")
    private String availableDays; // stored as comma-separated: "Monday,Wednesday,Friday"

    @Column(name = "working_hours")
    private String workingHours; // e.g. "09:00 AM - 04:00 PM"

    @Column(name = "consultation_fee")
    private String consultationFee;

    public Doctor() {}

    public Doctor(User user, String name, String specialization) {
        this.user = user;
        this.name = name;
        this.specialization = specialization;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getConsultationFee() { return consultationFee; }
    public void setConsultationFee(String consultationFee) { this.consultationFee = consultationFee; }
}
