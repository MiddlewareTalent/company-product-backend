package com.accesshr.emsbackend.Dto;

import com.accesshr.emsbackend.Entity.*;

public class EmployeeCompanyDetailsDTO {
    private EmployeeManager employeeManager;
    private ClientDetails clientDetails;

    // public EmployeeCompanyDetailsDTO(EmployeeManager employeeManager, ClientDetails clientDetails){
    //     this.employeeManager=employeeManager;
    //     this.clientDetails=clientDetails;
    // }

    public void setEmployeeManager(EmployeeManager employeeManager){
        this.employeeManager=employeeManager;
    }

    public EmployeeManager getEmployeeManager(){
        return employeeManager;
    }

    public void setClientDetails(ClientDetails clientDetails){
        this.clientDetails=clientDetails;
    }

    public ClientDetails getClientDetails(){
        return clientDetails;
    }
}
