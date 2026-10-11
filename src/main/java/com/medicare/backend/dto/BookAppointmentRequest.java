package com.medicare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookAppointmentRequest {

    @NotNull
    private Long doctorId;

    @NotBlank
    private String appointmentDate; // "2026-09-20"

    @NotBlank
    private String appointmentTime; // "10:00 AM"

    private String reason;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
