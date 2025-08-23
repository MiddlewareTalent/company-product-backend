package com.accesshr.emsbackend.Service.InvoiceService;

import com.accesshr.emsbackend.Dto.InvoiceDTO.ClientDTO;
import com.accesshr.emsbackend.Dto.InvoiceDTO.CountInvoicesDTO;
import com.accesshr.emsbackend.Dto.InvoiceDTO.InvoiceDTO;
import com.accesshr.emsbackend.Dto.InvoiceDTO.InvoiceItemDTO;
import com.accesshr.emsbackend.Entity.Invoice.Client;
import com.accesshr.emsbackend.Entity.Invoice.Invoice;
import com.accesshr.emsbackend.Entity.Invoice.InvoiceItem;
import com.accesshr.emsbackend.Entity.Invoice.InvoiceStatus;
import com.accesshr.emsbackend.Repo.InvoiceRepo.ClientRepository;
import com.accesshr.emsbackend.Repo.InvoiceRepo.InvoiceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final ClientRepository clientRepository;


    public InvoiceService(InvoiceRepository invoiceRepository, ClientRepository clientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
    }

    public Invoice createInvoice(InvoiceDTO invoiceDTO) {
        Invoice invoice = convertToEntity(invoiceDTO);
//        if(invoice.getClient()!=null && invoice.getClient().getId()>0){
//            System.out.println(invoice.getClient().getId()+" Client Id");
//            Client existingClient = clientRepository.findById(invoice.getClient().getId()).orElseThrow(() -> new RuntimeException("Client not found"));
//            invoice.setClient(existingClient);
//        } else if (invoice.getClient()!=null) {
//            Client newClient = invoice.getClient();
//            System.out.println(" Client Details "+invoice.getClient());
//            clientRepository.save(newClient);
//            invoice.setClient(newClient);
//        }
        if(invoice.getIssueDate()==null || invoice.getDueDate()==null){
            throw new RuntimeException("Invoice start date and end date must be provided.");
        }
        if(invoice.getDueDate().isBefore(invoice.getIssueDate())){
            throw new RuntimeException("Invoice end date cannot be before the start date.");
        }
        Client newClient = invoice.getClient();
        invoice.setInvoiceStatus(InvoiceStatus.PENDING);
        clientRepository.save(newClient);
        invoice.setClient(newClient);
        Invoice savedInvoice = invoiceRepository.save(invoice);

//            emailService.notifyClient(savedInvoice.getClient());
        return savedInvoice;
    }

    public Invoice paidInvoice(int id){
        Invoice approve = invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice Id not found"));
        approve.setInvoiceStatus(InvoiceStatus.PAID);
        invoiceRepository.save(approve);
        return approve;
    }

    public Invoice overDueInvoice(int id){
        Invoice overDue = invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice Id not found"));
        overDue.setInvoiceStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(overDue);
        return overDue;
    }

    public Invoice updateInvoice(int id, InvoiceDTO invoiceDTO) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice Id not found"));
        updateInvoiceFields(existingInvoice, invoiceDTO);
        return invoiceRepository.save(existingInvoice);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOverdueInvoices(){
        List<Invoice> pendingInvoices = invoiceRepository.findByInvoiceStatus(InvoiceStatus.PENDING);
        LocalDate today = LocalDate.now();
        for(Invoice invoice: pendingInvoices){
            if(invoice.getDueDate()!=null && invoice.getDueDate().isBefore(today)){
                invoice.setInvoiceStatus(InvoiceStatus.OVERDUE);
            }
        }
        invoiceRepository.saveAll(pendingInvoices);
    }

    public Invoice getInvoice(int id) {
        return invoiceRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Invoice Id not found"));
    }

    public List<CountInvoicesDTO> getAllInvoiceData(){
        List<CountInvoicesDTO> invoiceData = invoiceRepository.countAllInvoices();
        if (invoiceData.isEmpty()){
            throw new RuntimeException("No Invoice Data Found");
        }
        return invoiceData;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllWithItems();
    }

    private Invoice convertToEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setTax(dto.getTax());
        invoice.setCompanyName(dto.getCompanyName());
        invoice.setInvoiceType(dto.getInvoiceType());
        invoice.setInvoiceStatus(dto.getInvoiceStatus());
        invoice.setCountry(dto.getCountry());
        if (dto.getClient() != null) {
            Client client = new Client();
            client.setClientName(dto.getClient().getClientName());
            client.setClientEmail(dto.getClient().getClientEmail());
            client.setClientPhone(dto.getClient().getClientPhone());
            client.setClientAddress(dto.getClient().getClientAddress());
            invoice.setClient(client);
        }

        if (dto.getInvoiceItems() != null) {
            List<InvoiceItem> items = dto.getInvoiceItems().stream()
                    .map(itemDto -> {
                        InvoiceItem item = new InvoiceItem();
                        item.setDescription(itemDto.getDescription());
                        item.setQuantity(itemDto.getQuantity());
                        item.setUnitPrice(itemDto.getUnitPrice());
                        item.setTotalPrice(itemDto.getTotalPrice());
                        item.setInvoice(invoice);
                        return item;
                    })
                    .collect(Collectors.toList());
            invoice.setInvoiceItems(items);
        }

//        if (dto.getPayments() != null) {
//            List<Payment> payments = dto.getPayments().stream()
//                    .map(paymentDto -> {
//                        Payment payment = new Payment();
//                        payment.setAmount(paymentDto.getAmount());
//                        payment.setPaymentDate(paymentDto.getPaymentDate());
//                        payment.setPaymentMethod(paymentDto.getPaymentMethod());
//                        payment.setPaymentStatus(paymentDto.getPaymentStatus());
//                        payment.setTransactionId(paymentDto.getTransactionId());
//                        payment.setInvoice(invoice);
//                        return payment;
//                    })
//                    .collect(Collectors.toList());
//            invoice.setPayments(payments);
//        }

        return invoice;
    }

    public Long getInvoiceCountByMonthAndCompany(int month, int year, String clientName){
        return invoiceRepository.countInvoicesByClientAndMonth(month, year, clientName);
    }

    public List<Invoice> getInvoicesByStatus(InvoiceStatus invoiceStatus){
        try{
            return invoiceRepository.findByInvoiceStatus(invoiceStatus);
        }catch (Exception e){
            throw new RuntimeException("NO Invoice status Found"+e.getMessage());
        }
    }

    public List<Invoice> getInvoicesByStatuses(List<InvoiceStatus> statuses){
        try{
            return invoiceRepository.findByInvoiceStatusIn(statuses);
        } catch (Exception e) {
            throw new RuntimeException("No Invoices status found"+e.getMessage());
        }
    }

    public List<Invoice> getAllPendingInvoices(){
        return invoiceRepository.findByInvoiceStatus(InvoiceStatus.PENDING);
    }

    public List<Invoice> getAllPaidInvoices(){
        return invoiceRepository.findByInvoiceStatus(InvoiceStatus.PAID);
    }

    public List<Invoice> getAllOverdueInvoices(){
        return invoiceRepository.findByInvoiceStatus(InvoiceStatus.OVERDUE);
    }

    public List<Invoice> fetchByIssueDateAndDueDate(LocalDate issueDate, LocalDate dueDate){
        return invoiceRepository.fetchByIssueAndDueDates(issueDate,dueDate);
    }
    private void updateInvoiceFields(Invoice invoice, InvoiceDTO dto) {
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setTax(dto.getTax());
        invoice.setCompanyName(dto.getCompanyName());
        invoice.setInvoiceType(dto.getInvoiceType());
        invoice.setInvoiceStatus(dto.getInvoiceStatus());
        invoice.setCountry(dto.getCountry());
        if (dto.getInvoiceItems() != null) {
            invoice.getInvoiceItems().clear(); // Clear old items first
            invoice.getInvoiceItems().addAll(dto.getInvoiceItems().stream()
                    .map(itemDto -> {
                        InvoiceItem invoiceItem = new InvoiceItem();
                        invoiceItem.setDescription(itemDto.getDescription());
                        invoiceItem.setQuantity(itemDto.getQuantity());
                        invoiceItem.setUnitPrice(itemDto.getUnitPrice());
                        invoiceItem.setTotalPrice(itemDto.getTotalPrice());
                        invoiceItem.setInvoice(invoice);
                        return invoiceItem;
                    })
                    .collect(Collectors.toList()));
        }
    }

    public InvoiceDTO convertToDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setTax(invoice.getTax());
        dto.setCompanyName(invoice.getCompanyName());
        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setInvoiceStatus(invoice.getInvoiceStatus());
        dto.setCountry(invoice.getCountry());
        if (invoice.getClient() != null) {
            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setId(invoice.getClient().getId());
            clientDTO.setClientName(invoice.getClient().getClientName());
            clientDTO.setClientEmail(invoice.getClient().getClientEmail());
            clientDTO.setClientPhone(invoice.getClient().getClientPhone());
            clientDTO.setClientAddress(invoice.getClient().getClientAddress());
            dto.setClient(clientDTO);
        }

        if (invoice.getInvoiceItems() != null) {
            List<InvoiceItemDTO> itemDTOS = invoice.getInvoiceItems().stream()
                    .map(item -> {
                        InvoiceItemDTO itemDTO = new InvoiceItemDTO();
                        itemDTO.setDescription(item.getDescription());
                        itemDTO.setQuantity(item.getQuantity());
                        itemDTO.setUnitPrice(item.getUnitPrice());
                        itemDTO.setTotalPrice(item.getTotalPrice());
                        return itemDTO;
                    })
                    .collect(Collectors.toList());
            dto.setInvoiceItems(itemDTOS);
        }

//        if (invoice.getPayments() != null) {
//            List<PaymentDTO> paymentDTOS = invoice.getPayments().stream()
//                    .map(payment -> {
//                        PaymentDTO paymentDTO = new PaymentDTO();
//                        paymentDTO.setAmount(payment.getAmount());
//                        paymentDTO.setPaymentDate(payment.getPaymentDate());
//                        paymentDTO.setPaymentMethod(payment.getPaymentMethod());
//                        paymentDTO.setPaymentStatus(payment.getPaymentStatus());
//                        paymentDTO.setTransactionId(payment.getTransactionId());
//                        return paymentDTO;
//                    })
//                    .collect(Collectors.toList());
//            dto.setPayments(paymentDTOS);
//        }

        return dto;
    }
}
