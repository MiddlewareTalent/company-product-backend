package com.accesshr.emsbackend.EmployeeController;


import com.accesshr.emsbackend.Service.TenantSchemaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantSchemaService tenantService;

    public TenantController(TenantSchemaService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/{schemaName}")
    public ResponseEntity<String> createTenant(@PathVariable String schemaName) {
        try {
            tenantService.createTenant(schemaName);
            return ResponseEntity.ok("Schema and tables created for: " + schemaName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
