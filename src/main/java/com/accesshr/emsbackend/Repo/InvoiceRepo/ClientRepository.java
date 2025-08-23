package com.accesshr.emsbackend.Repo.InvoiceRepo;

import com.accesshr.emsbackend.Entity.Invoice.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client,Integer> {
}
