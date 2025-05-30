package com.accesshr.emsbackend.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class LeaveSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int SICK;
    private int VACATION;
    private int CASUAL;
    private int MARRIAGE;
    private int PATERNITY;
    private int MATERNITY;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSICK() {
        return SICK;
    }

    public void setSICK(int SICK) {
        this.SICK = SICK;
    }

    public int getVACATION() {
        return VACATION;
    }

    public void setVACATION(int VACATION) {
        this.VACATION = VACATION;
    }

    public int getCASUAL() {
        return CASUAL;
    }

    public void setCASUAL(int CASUAL) {
        this.CASUAL = CASUAL;
    }

    public int getMARRIAGE() {
        return MARRIAGE;
    }

    public void setMARRIAGE(int MARRIAGE) {
        this.MARRIAGE = MARRIAGE;
    }

    public int getPATERNITY() {
        return PATERNITY;
    }

    public void setPATERNITY(int PATERNITY) {
        this.PATERNITY = PATERNITY;
    }

    public int getMATERNITY() {
        return MATERNITY;
    }

    public void setMATERNITY(int MATERNITY) {
        this.MATERNITY = MATERNITY;
    }

    public int getOTHERS() {
        return OTHERS;
    }

    public void setOTHERS(int OTHERS) {
        this.OTHERS = OTHERS;
    }

    private int OTHERS;
}
