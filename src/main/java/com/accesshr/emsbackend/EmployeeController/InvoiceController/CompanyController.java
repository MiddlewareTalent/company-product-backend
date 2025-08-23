package com.accesshr.emsbackend.EmployeeController.InvoiceController;

import com.accesshr.emsbackend.Entity.Invoice.CompanyDetails;
import com.accesshr.emsbackend.Service.InvoiceService.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companyDetails")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<?> saveCompany(@RequestBody CompanyDetails companyDetails) {
        CompanyDetails saveDetails = companyService.saveCompanyDetails(companyDetails);
        return ResponseEntity.ok(saveDetails);
    }

    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<CompanyDetails>> getAllCompanyDetails() {
        List<CompanyDetails> allCompanyDetails = companyService.getAllCompanyDetails();
        return ResponseEntity.ok(allCompanyDetails);
    }

    @GetMapping(value = "/getData/{id}", produces = "application/json")
    public ResponseEntity<CompanyDetails> getCompanyDetails(@PathVariable int id) {
        CompanyDetails companyDetails = companyService.getCompanyDetails(id);
        return ResponseEntity.ok(companyDetails);
    }


    @GetMapping(value = "/getByCountry/{country}", produces = "application/json")
    public ResponseEntity<List<CompanyDetails>> findByCountry(@PathVariable String country){
        List<CompanyDetails> companyDetails = companyService.findByCountry(country);
        return ResponseEntity.ok(companyDetails);
    }

    @PutMapping("/updateById/{id}")
    public ResponseEntity<CompanyDetails> updateById(@PathVariable int id, @RequestBody CompanyDetails companyDetails){
        CompanyDetails updateDetails = companyService.updateCompanyById(id, companyDetails);
        return ResponseEntity.ok(updateDetails);
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<String> deleteById(@PathVariable int id){
        String companyDetails = companyService.deleteById(id);
        return ResponseEntity.ok(companyDetails);
    }
}
