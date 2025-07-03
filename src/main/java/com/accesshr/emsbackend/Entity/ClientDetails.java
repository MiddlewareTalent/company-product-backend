package com.accesshr.emsbackend.Entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class ClientDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;

    private String firstName;
    private String lastName;
    @Column(name = "schema_name")
    private String schemaName;
    private String email;
    private String country;
    private String serverUrl;
    private int noOfEmployees;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate starDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String plan;
    private double price;


   private boolean task=false;
   private boolean organizationChart=false;
   private boolean leaveManagement=false;
   private boolean timeSheet=false;
}
