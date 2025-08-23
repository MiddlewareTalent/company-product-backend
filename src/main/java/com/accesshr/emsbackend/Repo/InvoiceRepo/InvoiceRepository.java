package com.accesshr.emsbackend.Repo.InvoiceRepo;

import com.accesshr.emsbackend.Dto.InvoiceDTO.CountInvoicesDTO;
import com.accesshr.emsbackend.Entity.Invoice.Invoice;
import com.accesshr.emsbackend.Entity.Invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    @Query("SELECT COUNT(i) FROM Invoice i " +
            "WHERE FUNCTION('MONTH', i.issueDate) = :month " +
            "AND FUNCTION('YEAR', i.issueDate) = :year " +
            "AND i.client.id IN (SELECT c.id FROM Client c WHERE c.clientName = :clientName)")
    Long countInvoicesByClientAndMonth(@Param("month") int month,
                                       @Param("year") int year,
                                       @Param("clientName") String clientName);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.invoiceItems WHERE i.invoiceStatus=:invoiceStatus")
    List<Invoice> findByInvoiceStatus(@Param("invoiceStatus") InvoiceStatus invoiceStatus);

    List<Invoice> findByInvoiceStatusIn(List<InvoiceStatus> statuses);


    @Query("SELECT new com.accesshr.emsbackend.Dto.InvoiceDTO.CountInvoicesDTO(i.invoiceStatus, COUNT(i)) FROM Invoice i GROUP BY i.invoiceStatus ORDER BY i.invoiceStatus ASC")
    List<CountInvoicesDTO> countAllInvoices();

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.invoiceItems WHERE i.issueDate BETWEEN :issueDate AND :dueDate AND i.dueDate BETWEEN :issueDate AND :dueDate")
    List<Invoice> fetchByIssueAndDueDates(@Param("issueDate") LocalDate issueDate,
                                          @Param("dueDate") LocalDate dueDate);


    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.invoiceItems")
    List<Invoice> findAllWithItems();

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.invoiceItems WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") int id);


}
