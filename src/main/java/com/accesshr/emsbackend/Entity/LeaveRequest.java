package com.accesshr.emsbackend.Entity;

import com.accesshr.emsbackend.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employeeId;
    private String firstName;
    private String lastName;


    private String email;

    private String managerId;
    
    private String managerEmail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate leaveStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate leaveEndDate;

    private String leaveReason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public LocalDate getLeaveStartDate() {
        return leaveStartDate;
    }

    public void setLeaveStartDate(LocalDate leaveStartDate) {
        this.leaveStartDate = leaveStartDate;
    }

    public LocalDate getLeaveEndDate() {
        return leaveEndDate;
    }

    public void setLeaveEndDate(LocalDate leaveEndDate) {
        this.leaveEndDate = leaveEndDate;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }

    public com.accesshr.emsbackend.Entity.LeaveRequest.LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(com.accesshr.emsbackend.Entity.LeaveRequest.LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public boolean isLOP() {
        return LOP;
    }

    public void setLOP(boolean LOP) {
        this.LOP = LOP;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getComments() {
        return comments;
    }

    public Double getLopDays() {
        return lopDays;
    }

    public void setLopDays(Double lopDays) {
        this.lopDays = lopDays;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMedicalDocument() {
        return medicalDocument;
    }

    public void setMedicalDocument(String medicalDocument) {
        this.medicalDocument = medicalDocument;
    }

    public com.accesshr.emsbackend.Entity.LeaveRequest.LeaveStatus getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(com.accesshr.emsbackend.Entity.LeaveRequest.LeaveStatus leaveStatus) {
        this.leaveStatus = leaveStatus;
    }

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;
    private boolean LOP;
    private Double lopDays;
    private Double duration;
    private String durationType;
    private String comments;

    private String medicalDocument;

    @Enumerated(EnumType.STRING)
    private LeaveStatus leaveStatus;

    public enum LeaveStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum LeaveType{
        SICK,
        VACATION,
        CASUAL,
        MARRIAGE,
        PATERNITY,
        MATERNITY,
        OTHERS
    }


    public void calculateDuration(List<LocalDate> nationalHolidays) {
        if (leaveStartDate == null || leaveEndDate == null) {
            throw new ResourceNotFoundException("Leave start and end dates cannot be null.");
        }

        if (leaveEndDate.isBefore(leaveStartDate)) {
            throw new ResourceNotFoundException("Leave end date cannot be before the start date.");
        }

        // Calculate business days excluding weekends and national holidays
        double businessDays = calculateBusinessDays(leaveStartDate, leaveEndDate, nationalHolidays);

        // Each business day is equivalent to 8 working hours
        int workingHoursPerDay = 8;
        int totalWorkingHours = (int) businessDays * workingHoursPerDay;

        // Store the calculated duration
        this.duration =  businessDays; // Total business days
        this.durationType = "Days"; // Indicates the unit used
    }

    public double calculateBusinessDays(LocalDate startDate, LocalDate endDate, List<LocalDate> nationalHolidays) {
        return startDate.datesUntil(endDate.plusDays(1)) // Inclusive of endDate
                .filter(date -> !isWeekend(date) && !nationalHolidays.contains(date))
                .count();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
