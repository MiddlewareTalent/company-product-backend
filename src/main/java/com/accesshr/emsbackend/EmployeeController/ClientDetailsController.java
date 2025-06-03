package com.accesshr.emsbackend.EmployeeController;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/clientDetails")
public class ClientDetailsController {

    private final ClientDetailsService clientDetailsService;

    public ClientDetailsController(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = clientDetailsService;
    }

    @PostMapping
    public ResponseEntity<ClientDetails> saveClientDetails(@RequestBody ClientDetails clientDetails) {
        ClientDetails saveClient = clientDetailsService.createClient(clientDetails);
        return ResponseEntity.ok(saveClient);
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<ClientDetails>> getAllClientDetails() {
        List<ClientDetails> allClientDetails = clientDetailsService.getAllClientDetails();
        return ResponseEntity.ok(allClientDetails);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ClientDetails> updateClientDetails(@PathVariable Long id, @RequestBody ClientDetails clientDetails) {
        ClientDetails updateClient = clientDetailsService.updateClientDetails(id, clientDetails);
        return ResponseEntity.ok(updateClient);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ClientDetails> getClientDetailsById(@PathVariable Long id) {
        ClientDetails clientDetails = clientDetailsService.getClientDetailsById(id);
        return ResponseEntity.ok(clientDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClientDetails(@PathVariable Long id) {
        String deleteClient = clientDetailsService.deleteClientDetails(id);
        return ResponseEntity.ok(deleteClient);
    }

}
