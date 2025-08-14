package com.accesshr.emsbackend.Entity;

import jakarta.persistence.*;


@Entity
public class LeaveSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String leaveType;
    private double noOfDays;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public double getNoOfDays() {
        return noOfDays;
    }

    public void setNoOfDays(double noOfDays) {
        this.noOfDays = noOfDays;
    }
}
