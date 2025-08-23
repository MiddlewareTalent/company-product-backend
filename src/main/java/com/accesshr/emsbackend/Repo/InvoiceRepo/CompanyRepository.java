package com.accesshr.emsbackend.Repo.InvoiceRepo;

import com.accesshr.emsbackend.Entity.Invoice.CompanyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyDetails, Integer> {

    List<CompanyDetails> findByCountry(String country);
}
