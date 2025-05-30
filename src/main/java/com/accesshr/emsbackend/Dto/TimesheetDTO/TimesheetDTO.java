package com.accesshr.emsbackend.Dto.TimesheetDTO;

import com.accesshr.emsbackend.Entity.Timesheet;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TimesheetDTO {
    private Long id;  // ID of the timesheet
    private String employeeId;  // ID of the employee
    private String employeeName;
    private double numberOfHours;  // Normal working hours

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

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getNumberOfHours() {
        return numberOfHours;
    }

    public void setNumberOfHours(double numberOfHours) {
        this.numberOfHours = numberOfHours;
    }

    public double getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(double extraHours) {
        this.extraHours = extraHours;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public void setReportingManager(String reportingManager) {
        this.reportingManager = reportingManager;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isOnCallSupport() {
        return onCallSupport;
    }

    public void setOnCallSupport(boolean onCallSupport) {
        this.onCallSupport = onCallSupport;
    }

    public Timesheet.Status getStatus() {
        return status;
    }

    public void setStatus(Timesheet.Status status) {
        this.status = status;
    }

    public double getTotalNumberOfHours() {
        return totalNumberOfHours;
    }

    public void setTotalNumberOfHours(double totalNumberOfHours) {
        this.totalNumberOfHours = totalNumberOfHours;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    private double extraHours;  // Extra hours worked
    private String reportingManager;  // Reporting manager's name
    private String clientName;  // Client's name
    private String projectName;  // Name of the project
    private String taskType;  // Task type name
    private String workLocation;  // Work location
    private String taskDescription;  // Description of the task
    private String emailId;  // Employee's email ID
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;  // Start date for the task
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private boolean onCallSupport;  // On-call support indicator
    private Timesheet.Status status;
    private double totalNumberOfHours;
    private String comments;
    private String manager;
    private String managerId;



}