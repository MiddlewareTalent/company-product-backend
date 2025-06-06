package com.accesshr.emsbackend.Entity;
import jakarta.persistence.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;


@Entity
public class JobRoles {
    @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="jobRoleId")
    int jobRoleId;
    @Column (name="jobRole")
    String jobRole;

    public JobRoles() {
    }

    public JobRoles(int jobRoleId, String jobRole){
        this.jobRoleId=jobRoleId;
        this.jobRole=jobRole;
    }

    public int getJoRoleId(){
        return jobRoleId;
    }

    public void setJobRoleId(int jobRoleId){
        this.jobRoleId=jobRoleId;
    }

    public String getJobRole(){
        return jobRole;
    }

    public void setJobRole(String jobRole){
        this.jobRole=jobRole;
    }
    
}
