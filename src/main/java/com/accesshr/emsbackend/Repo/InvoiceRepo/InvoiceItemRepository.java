package com.accesshr.emsbackend.Repo.InvoiceRepo;

import com.accesshr.emsbackend.Entity.Invoice.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Integer> {
}
