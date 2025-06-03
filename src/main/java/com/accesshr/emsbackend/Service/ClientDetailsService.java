package com.accesshr.emsbackend.Service;

import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Repo.ClientDetailsRepository;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Service
public class ClientDetailsService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private final ClientDetailsRepository ClientDetailsRepository;
    private final DataSource dataSource;

    public ClientDetailsService(com.accesshr.emsbackend.Repo.ClientDetailsRepository clientDetailsRepository, DataSource dataSource) {
        ClientDetailsRepository = clientDetailsRepository;
        this.dataSource = dataSource;
    }


    public ClientDetails createClient(ClientDetails clientDetails) {
        return ClientDetailsRepository.save(clientDetails);
    }

    public List<ClientDetails> getAllClientDetails() {
        return ClientDetailsRepository.findAll();
    }

    public ClientDetails getClientDetailsById(Long id){
        return ClientDetailsRepository.findById(id).orElseThrow(()-> new RuntimeException("Client details not found"));
    }

    public ClientDetails updateClientDetails(Long id,ClientDetails clientDetails) {
        ClientDetails update = ClientDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("Client details not found"));
        if (update!=null){
            update.setCompanyName(clientDetails.getCompanyName());
            update.setEmail(clientDetails.getEmail());
            update.setSchemaName(clientDetails.getSchemaName());
//            update.setTask(clientDetails.isTask());
//            update.setLeaveManagement(clientDetails.isLeaveManagement());
//            update.setOrganizationChart(clientDetails.isOrganizationChart());
//            update.setTimeSheet(clientDetails.isTimeSheet());
            ClientDetailsRepository.save(update);
        }
        return null;
    }

    public String deleteClientDetails(Long id){
        ClientDetails client = ClientDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("Client details not found"));
        String schemaName = client.getSchemaName().toLowerCase();
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            stmt.executeUpdate("DROP SCHEMA IF EXISTS `" + schemaName + "`");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete tenant schema: " + e.getMessage(), e);
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
            schemaName=schemaName.replace("_","-")+"-container";
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(schemaName);
            if (containerClient.exists()) {
                containerClient.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete Azure blob container: " + e.getMessage(), e);
        }

        ClientDetailsRepository.delete(client);
        return "Client and associated resources deleted successfully";
    }
}
