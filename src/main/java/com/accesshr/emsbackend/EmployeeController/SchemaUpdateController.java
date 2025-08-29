package com.accesshr.emsbackend.EmployeeController;

import com.accesshr.emsbackend.Entity.CountryServerConfig;
import com.accesshr.emsbackend.Service.TenantSchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/schema")
public class SchemaUpdateController {

    private final TenantSchemaService tenantSchemaService;

    public SchemaUpdateController(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    @PostMapping(value = "/updateAll/{country}", produces = "application/json")
    public ResponseEntity<String> updateSchemasByCountry(@PathVariable String country){
        try {
            tenantSchemaService.updateSchemasForCountry(country);
            return ResponseEntity.ok("All tenant schemas updated successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update schemas: "+e.getMessage());
        }
    }

    @PostMapping("/update/{schemaName}/{country}")
    public ResponseEntity<String> updateSingleSchema(@PathVariable String schemaName, @PathVariable String country){
        try{
            CountryServerConfig config = CountryServerConfig.valueOf(country.toUpperCase());
            tenantSchemaService.createTablesInSchema(schemaName, config);
            return ResponseEntity.ok("Schema updated "+ schemaName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update schema "+schemaName+" : "+e.getMessage());
        }
    }

    @PostMapping(value = "/updateAll", produces = "application/json")
    public ResponseEntity<String> updateAllSchemas(@RequestParam(required = false) String country) {
        try {
            if (country != null) {
                tenantSchemaService.updateSchemasForCountry(country);
                return ResponseEntity.ok("Schemas updated successfully for " + country);
            } else {
                tenantSchemaService.updateAllSchemas();
                return ResponseEntity.ok("All tenant schemas updated successfully.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update schemas: " + e.getMessage());
        }
    }

}
