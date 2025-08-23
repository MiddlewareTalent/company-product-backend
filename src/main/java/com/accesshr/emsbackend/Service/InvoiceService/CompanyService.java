package com.accesshr.emsbackend.Service.InvoiceService;

import com.accesshr.emsbackend.Entity.Invoice.CompanyDetails;
import com.accesshr.emsbackend.Repo.InvoiceRepo.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyDetails saveCompanyDetails(CompanyDetails companyDetails) {
        return companyRepository.save(companyDetails);
    }

    public List<CompanyDetails> getAllCompanyDetails() {
        return companyRepository.findAll();
    }

    public CompanyDetails getCompanyDetails(int id) {
        CompanyDetails companyDetails = companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
        return companyDetails;
    }

    public List<CompanyDetails> findByCountry(String country){
        try {
            List<CompanyDetails> companyDetails = companyRepository.findByCountry(country);
            return companyDetails;
        } catch (Exception e) {
            throw new RuntimeException("Country not Found: "+e.getMessage());
        }

    }

    public CompanyDetails updateCompanyById(int id, CompanyDetails companyDetails){
        CompanyDetails update = companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));
        if (update!=null){
            update.setCompanyName(companyDetails.getCompanyName());
            update.setCompanyCode(companyDetails.getCompanyCode());
            update.setCompanyEmail(companyDetails.getCompanyEmail());
            update.setCountry(companyDetails.getCountry());
            update.setGstNo(companyDetails.getGstNo());
            update.setCompanyAddress(companyDetails.getCompanyAddress());
            companyRepository.save(update);
        }
        return null;
    }

    public String deleteById(int id){
        CompanyDetails companyDetails = companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not Found"));
        companyRepository.delete(companyDetails);
        return "Company Deleted Successfully";
    }
}
