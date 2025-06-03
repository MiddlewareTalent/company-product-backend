package com.accesshr.emsbackend.Entity;

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

//    private boolean task=false;
//    private boolean organizationChart=false;
//    private boolean leaveManagement=false;
//    private boolean timeSheet=false;
}
