package com.accesshr.emsbackend.EmployeeController;

import com.accesshr.emsbackend.Dto.EmployeeManagerDTO;
import com.accesshr.emsbackend.Dto.LoginDTO;
import com.accesshr.emsbackend.EmployeeController.Config.TenantContext;
import com.accesshr.emsbackend.Entity.ClientDetails;
import com.accesshr.emsbackend.Entity.EmployeeManager;
import com.accesshr.emsbackend.Repo.EmployeeManagerRepository;
import com.accesshr.emsbackend.Service.ClientDetailsService;
import com.accesshr.emsbackend.Service.EmployeeManagerService;
import com.accesshr.emsbackend.Service.TenantSchemaService;
import com.accesshr.emsbackend.response.LoginResponse;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;


import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employeeManager")
@CrossOrigin(origins = "https://company-product-frontend.azurewebsites.net") // Adjust as needed for your frontend
public class EmployeeManagerController {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    private final EmployeeManagerService employeeManagerService;
    private final EmployeeManagerRepository employeeManagerRepository;
    private final TenantSchemaService tenantSchemaService;
    private final ClientDetailsService clientDetailsService;

    public EmployeeManagerController(EmployeeManagerService employeeManagerService, EmployeeManagerRepository employeeManagerRepository, TenantSchemaService tenantSchemaService, ClientDetailsService clientDetailsService) {
        this.employeeManagerService = employeeManagerService;
        this.employeeManagerRepository = employeeManagerRepository;
        this.tenantSchemaService=tenantSchemaService;
        this.clientDetailsService = clientDetailsService;
    }


    // Add Employee method (used by admins to add employees)
    @PostMapping(value = "/add", produces = "application/json")
    public ResponseEntity<?> addEmployee(
            @Valid @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("country") String country,
            @RequestParam("streetAddress") String streetAddress,
            @RequestParam("city") String city,
            @RequestParam("region") String region,
            @RequestParam("postalCode") String postalCode,
            @RequestParam("workingCountry") String workingCountry,
            @RequestParam("nationalInsuranceNumber") String nationalInsuranceNumber,
            @RequestParam("employeeId") String employeeId,
            @RequestParam("corporateEmail") String corporateEmail,
            @RequestParam("jobRole") String jobRole,
            @RequestParam("employmentStatus") String employmentStatus,
            @RequestParam("reportingTo") String reportingTo,
            @RequestParam("role") String role,
            @RequestParam(value = "dateOfBirth", required = false) LocalDate dateOfBirth,
            @RequestParam(value = "dateOfJoining", required = false) LocalDate dateOfJoining,
            @RequestParam(value = "task", required = false) boolean task,
            @RequestParam(value = "organizationChart", required = false) boolean organizationChart,
            @RequestParam(value = "timeSheet", required = false) boolean timeSheet,
            @RequestParam(value = "leaveManagement", required = false) boolean leaveManagement,
            @RequestParam(value = "identityCard", required = false) MultipartFile identityCard,
            @RequestParam(value = "visa", required = false) MultipartFile visa,
            @RequestParam(value = "otherDocuments", required = false) MultipartFile otherDocuments,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            EmployeeManagerDTO employeeManagerDTO = new EmployeeManagerDTO();
            employeeManagerDTO.setFirstName(firstName);
            employeeManagerDTO.setLastName(lastName);
            employeeManagerDTO.setEmail(email);
            employeeManagerDTO.setCountry(country);
            employeeManagerDTO.setStreetAddress(streetAddress);
            employeeManagerDTO.setCity(city);
            employeeManagerDTO.setRegion(region);
            employeeManagerDTO.setPostalCode(postalCode);
            employeeManagerDTO.setWorkingCountry(workingCountry);
            employeeManagerDTO.setNationalInsuranceNumber(nationalInsuranceNumber);
            employeeManagerDTO.setEmployeeId(employeeId);
            employeeManagerDTO.setCorporateEmail(corporateEmail);
            employeeManagerDTO.setJobRole(jobRole);
            employeeManagerDTO.setDateOfJoining(dateOfJoining);
            employeeManagerDTO.setDateOfBirth(dateOfBirth);
            employeeManagerDTO.setTask(task);
            employeeManagerDTO.setOrganizationChart(organizationChart);
            employeeManagerDTO.setTimeSheet(timeSheet);
            employeeManagerDTO.setLeaveManagement(leaveManagement);
            employeeManagerDTO.setEmploymentStatus(employmentStatus);
            employeeManagerDTO.setReportingTo(reportingTo);
            employeeManagerDTO.setRole(role);
            employeeManagerDTO.setProfilePhoto(saveOptionalFile(profilePhoto, "profilePhoto"));
            // Save files and update DTO fields for certificates
            employeeManagerDTO.setIdentityCard(uploadFIle(identityCard, "nationalCard"));
            employeeManagerDTO.setVisa(saveOptionalFile(visa, "visa"));
            employeeManagerDTO.setOtherDocuments(saveOptionalFile(otherDocuments, "otherDocuments"));

            if (employeeManagerService.existsByCorporateEmail(corporateEmail)) {
                throw new RuntimeException("Corporate email already exists: " + corporateEmail);
            }
            EmployeeManagerDTO employeeManager = employeeManagerService.addEmployee(employeeManagerDTO);

            return ResponseEntity.ok(employeeManager);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    // Registration endpoint (for Admins)
    @PostMapping(value = "/register/{company}", produces = "application/json")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("task") boolean task,
            @RequestParam("timeSheet") boolean timeSheet,
            @RequestParam("organizationChart") boolean organizationChart,
            @RequestParam("leaveManagement") boolean leaveManagement,@PathVariable String company) {

        EmployeeManagerDTO employeeManagerDTO = new EmployeeManagerDTO();
        employeeManagerDTO.setFirstName(firstName);
        employeeManagerDTO.setLastName(lastName);
        employeeManagerDTO.setEmail(email);
        employeeManagerDTO.setCorporateEmail(email); // Set corporate email to the same email for registration
        employeeManagerDTO.setRole("admin"); // Default role for admin
        employeeManagerDTO.setOrganizationChart(organizationChart);
        employeeManagerDTO.setTask(task);
        employeeManagerDTO.setLeaveManagement(leaveManagement);
        employeeManagerDTO.setTimeSheet(timeSheet);
        employeeManagerDTO.setPassword(password); // Set plain text password

        ClientDetails clientDetails=new ClientDetails();


        try {
            
            tenantSchemaService.createTenant(company);
            String companySchema=company.replace(" ", "_");
            clientDetails.setFirstName(firstName);
            clientDetails.setLastName(lastName);
            clientDetails.setCompanyName(company);
            clientDetails.setEmail(email);
            clientDetails.setSchemaName(companySchema);
            clientDetailsService.createClient(clientDetails);
            TenantContext.setTenantId(companySchema);
            EmployeeManagerDTO registeredAdmin = employeeManagerService.addAdmin(companySchema,employeeManagerDTO);
            return ResponseEntity.ok(registeredAdmin);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("nationalCard") MultipartFile nationalCard) {
        try {
            String blobName = uploadFIle(nationalCard, "nationalCard");
            return ResponseEntity.ok(blobName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public void init() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

//    public String uploadFIle(MultipartFile file, String caption) throws IOException {
//        String blobFilename = file.getOriginalFilename();
//
//        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
//                .connectionString(connectionString)
//                .buildClient();
//        BlobClient blobClient = blobServiceClient
//                .getBlobContainerClient(containerName)
//                .getBlobClient(blobFilename);
//
//        blobClient.upload(file.getInputStream(), file.getSize(), true);
//        String fileUrl = blobClient.getBlobUrl();
//
//        return fileUrl;
//    }

    public String uploadFIle(MultipartFile file, String caption) throws IOException {
    String tenantId = TenantContext.getTenantId();
    tenantId = tenantId.replace("_", "-");

    if (tenantId == null || tenantId.isBlank()) {
        throw new IllegalStateException("Tenant ID is missing from context");
    }

    String containerForTenant = tenantId.toLowerCase() + "-container";
    String blobFilename = file.getOriginalFilename();

    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();

    BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerForTenant);

    // Create container if it does not exist, with public access to blobs
    if (!containerClient.exists()) {
    containerClient.createWithResponse(
        null, // Metadata
        PublicAccessType.BLOB,
        null, // Request conditions
        Context.NONE
    );
}


    BlobClient blobClient = containerClient.getBlobClient(blobFilename);
    blobClient.upload(file.getInputStream(), file.getSize(), true);

    return blobClient.getBlobUrl();
}



    private String saveFile(MultipartFile file, String fileType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The file cannot be null or empty.");
        }

        // Create a BlobContainerClient
        BlobContainerClient blobContainerClient = new BlobClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient().getContainerClient();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Original filename cannot be null.");
        }

        String blobName = fileType + "-" + originalFilename;

        // Log the blob name before uploading
        System.out.println("Uploading to blob name: " + blobName);

        // Upload the file to Blob Storage
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobName; // Return the blob name or URL if needed
    }


    // Save optional file to Azure Blob Storage
    private String saveOptionalFile(MultipartFile file, String fileType) throws IOException {
        if (file != null && !file.isEmpty()) {
            return uploadFIle(file, fileType);
        }
        return null;
    }

    // New login endpoint
    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginDTO loginDTO) {
        LoginResponse response = employeeManagerService.loginEmployee(loginDTO);
        return new ResponseEntity<>(response, response.getStatus() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }


    // Fetch all employees
    @GetMapping(value = "/employees", produces = "application/json")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<EmployeeManagerDTO> employees = employeeManagerService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch employees: " + e.getMessage());
        }
    }

//    // Fetch a specific employee by ID
//    @GetMapping(value = "/employees/{employeeId}", produces = "application/json")
//    public ResponseEntity<?> getEmployeesById(@PathVariable("employeeId") String  employeeId) {
//        try {
//            EmployeeManagerDTO employee = employeeManagerService.getEmployeeDataById(employeeId);
//            if (employee != null) {
//                return ResponseEntity.ok(employee);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch employee: " + e.getMessage());
//        }
//    }

    @GetMapping(value = "/employees/{id}", produces = "application/json")
    public ResponseEntity<?> getEmployeeById(@PathVariable("id") int id) {
        try {
            EmployeeManagerDTO employeeId = employeeManagerService.getById(id);
            if (employeeId != null) {
                return ResponseEntity.ok(employeeId);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch employee: " + e.getMessage());
        }
    }

    // Delete an employee by ID
//    @DeleteMapping("/employees/{id}")
//    public ResponseEntity<String> deleteEmployee(@PathVariable("employeeId") int employeeId) {
//        try {
//            boolean isDeleted = employeeManagerService.deleteEmployeeById(employeeId);
//            if (isDeleted) {
//                return ResponseEntity.ok("Employee deleted successfully");
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete employee: " + e.getMessage());
//        }
//    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEmployeeData(@PathVariable("id") int id, EmployeeManagerDTO employeeManagerDTO) {
        System.out.println(employeeManagerDTO.isOrganizationChart());
        System.out.println(employeeManagerDTO.isTask());
        EmployeeManagerDTO updateEmp = employeeManagerService.updateEmployee(id, employeeManagerDTO);
        try {
            return ResponseEntity.ok(updateEmp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update employee: " + e.getMessage());
        }
    }

//    @DeleteMapping("/employees/{id}")
//    public ResponseEntity<String> deleteEmployee(@PathVariable("id") int id) {
//        try {
//            boolean isDeleted = employeeManagerService.deleteById(id);
//            if (isDeleted) {
//                return ResponseEntity.ok("Employee deleted successfully");
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
//            }
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete employee: " + e.getMessage());
//        }
//    }


    // New endpoint to get file size
    @GetMapping("/fileSize")
    public ResponseEntity<Map<String, Long>> getFileSize(@RequestParam String fileName) {
        try {
            BlobContainerClient blobContainerClient = new BlobClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .buildClient().getContainerClient();

            // Get the blob properties
            BlobProperties properties = blobContainerClient.getBlobClient(fileName).getProperties();

            // Prepare response with file size
            Map<String, Long> response = new HashMap<>();
            response.put("size", properties.getBlobSize()); // Size in bytes
            return ResponseEntity.ok(response);
        } catch (BlobStorageException e) {
            // Handle not found error
            if (e.getStatusCode() == 404) {
                Map<String, Long> response = new HashMap<>();
                response.put("size", 0L); // File not found, size is 0
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            // Handle other errors
            Map<String, Long> response = new HashMap<>();
            response.put("size", 0L); // Error occurred, size is 0
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            // Handle any other exceptions
            Map<String, Long> response = new HashMap<>();
            response.put("size", 0L); // Error occurred, size is 0
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping(value = "/exists/{employeeId}", produces = "application/json")
    public Boolean checkEmployeeExists(@PathVariable String employeeId) {
        boolean exists = employeeManagerService.isEmployeeIdPresent(employeeId);

        if (exists) {
            return true;
        } else {
            return false;
        }
    }

    // Method to find the origin employee by their empId
    @GetMapping(value = "/origin/{employeeId}", produces = "application/json")
    public ResponseEntity<List<EmployeeManager>> getOriginEmployee(@PathVariable String employeeId) throws Exception {
        List<EmployeeManager> reportingChain = employeeManagerService.findOrigin(employeeId);

        if (reportingChain.isEmpty()) {
            return ResponseEntity.notFound().build();  // Return 404 if no employee or chain found
        }

        return ResponseEntity.ok(reportingChain);  // Return the list of employees in the reporting chain
    }


    @GetMapping(value = "/reporting-to/{employeeId}/{workingCountry}", produces = "application/json")
    public ResponseEntity<List<EmployeeManager>> getEmployeesReportingTo(@PathVariable String employeeId, @PathVariable String workingCountry) throws Exception {
        List<EmployeeManager> reportingEmployees = employeeManagerService.reportingToList(employeeId, workingCountry);

        if (reportingEmployees.isEmpty()) {
            // Throw an exception if no employees are found
            throw new Exception("No employees found reporting to ID: " + employeeId);
        }

        return ResponseEntity.ok(reportingEmployees); // Return the list with 200 OK
    }

    @GetMapping(value = "/myColleague/{managerId}", produces = "application/json")
    public ResponseEntity<List<EmployeeManager>> getMyColleague(@PathVariable String managerId) throws Exception {
        List<EmployeeManager> myColleague = employeeManagerService.getAllMyColleagues(managerId);//
        if (myColleague.isEmpty()) {
            throw new Exception("no employees found to ID: " + managerId);
        }
        return ResponseEntity.ok(myColleague);
    }

    @GetMapping(value = "/getEmployee/{employeeId}", produces = "application/json")
    public EmployeeManager getEmployeeById(@PathVariable String employeeId) {
        EmployeeManager employeeManager = employeeManagerService.getEmployeeById(employeeId);
        return employeeManager;
    }

    @GetMapping(value = "/alsoWorkingWith/{employeeId}/{workingCountry}", produces = "application/json")
    public List<EmployeeManager> getAlsoWorkingWith(@PathVariable String employeeId, @PathVariable String workingCountry) throws Exception {
        List<EmployeeManager> emp = employeeManagerService.alsoWorkingWith(employeeId, workingCountry);
        return emp;
    }

    @PostMapping("/reset-password/{employeeId}/{newPassword}")
    public ResponseEntity<String> changePassword(@PathVariable String employeeId, @PathVariable String newPassword) {
        try {
            // Call the service method to change the password
            employeeManagerService.changePassword(employeeId, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            // Return a bad request response if an error occurs
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody LoginDTO loginDTO) {
        try {
            boolean password = employeeManagerService.changePassword(loginDTO.getEmail(), loginDTO.getOldPassword(), loginDTO.getNewPassword());
            return ResponseEntity.ok(password);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to change password: " + e.getMessage());
        }
    }

    @GetMapping(value = "/getEmployeesByWorkingCountry/{workingCountry}", produces = "application/json")
    public List<EmployeeManager> getEmployeesByWorkingCountry(@PathVariable String workingCountry) {
        return employeeManagerService.getEmployeesByWorkingCountry(workingCountry);
    }

    @GetMapping(value = "/getEmployeesForTasks/{employeeId}", produces = "application/json")
    public List<EmployeeManager> getReportingEmployeesForTasks(@PathVariable String employeeId) {
        return employeeManagerService.getReportingEmployeesForTasks(employeeId);
    }

    private void deleteBlobs(List<String> blobUrls) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        for (String blobUrl : blobUrls) {
            if (blobUrl != null && !blobUrl.trim().isEmpty()) {
                try {
                    URL url = new URL(blobUrl);
                    String blobName = url.getPath().substring(url.getPath().indexOf(containerName) + containerName.length() + 1);

                    BlobClient blobClient = containerClient.getBlobClient(blobName);
                    if (blobClient.exists()) {
                        blobClient.delete();
                        System.out.println("Successfully deleted blob: " + blobUrl);
                    } else {
                        System.out.println("Blob not found: " + blobUrl);
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting blob: " + blobUrl + " - " + e.getMessage());
                }
            }
        }
    }

    private void deleteBlob(String blobUrl) {
        String tenantId = TenantContext.getTenantId();
        tenantId=tenantId.replace("_", "-");
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Tenant ID is missing from context");
        }
        String containerForTenant = tenantId.toLowerCase() + "-container";
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerForTenant);

        if (blobUrl != null && !blobUrl.trim().isEmpty()) {
            try {
                URL url = new URL(blobUrl);
//                String blobName = url.getPath().substring(url.getPath().indexOf(containerName) + containerName.length() + 1);
                String blobName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
                BlobClient blobClient = containerClient.getBlobClient(blobName);
                if (blobClient.exists()) {
                    blobClient.delete();
                    System.out.println("Successfully deleted blob: " + blobUrl);
                }
            } catch (Exception e) {
                System.err.println("Error deleting blob: " + blobUrl + " - " + e.getMessage());
            }
        }

    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable int id) {


        EmployeeManagerDTO employee = employeeManagerService.getById(id);
        System.out.println(employee);

        boolean isDeleted = employeeManagerService.deleteById(id);

        if (isDeleted) {
            // Delete associated files in Azure Blob Storage
            deleteBlobs(Arrays.asList(employee.getIdentityCard(), employee.getVisa(), employee.getOtherDocuments()));

            return ResponseEntity.ok("Employee and associated files deleted successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to delete employee");
        }
    }


    public boolean deleteDocument(String employeeId, EmployeeManager employee) {
        if (employee == null) {
            throw new RuntimeException("Employee not found with id: " + employeeId);
        } else {
            String profileURL = employee.getProfilePhoto();
            if (profileURL == null) {
                throw new RuntimeException("Profile photo not found");
            } else {
                deleteBlob(profileURL);
                return true;
            }
        }
    }

    @PutMapping(value = "/ProfilePhotoUpdate", produces = "application/json")
    public ResponseEntity<?> updateProfilePhoto(
            @Valid
            @RequestParam("employeeId") String employeeId,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto) {
        EmployeeManager employee = employeeManagerRepository.findByEmployeeId(employeeId);
        if(employee.getProfilePhoto()!=null){
            deleteDocument(employeeId, employee);
        }
        try {
            employee.setEmployeeId(employeeId);
            employee.setProfilePhoto(saveOptionalFile(profilePhoto,"profilePhoto"));
            EmployeeManager employeeManager = employeeManagerService.changeProfilePhoto(employeeId, employee);
            return ResponseEntity.ok(employeeManager);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    //Delete Profile 
    @DeleteMapping(value = "/ProfilePhotoDelete", produces = "application/json")
    public ResponseEntity<?> deleteProfilePhoto (
            @Valid
            @RequestParam("employeeId") String employeeId){
        EmployeeManager employee = employeeManagerRepository.findByEmployeeId(employeeId);
        deleteDocument(employeeId, employee);
        employee.setProfilePhoto(null);
        return ResponseEntity.ok("Profile Photo Deleted Successfully");
    }




    @GetMapping(value = "/employeesByOrder", produces = "application/json")
    public ResponseEntity<?> getAllEmployeesByOrder() {
        try {
            List<EmployeeManager> employees = employeeManagerService.getAllEmployeesByOrder();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch employees: " + e.getMessage());
        }
    }

    @GetMapping(value = "/AdminsAndManagers", produces = "application/json")
    public ResponseEntity<?> getAllAdminsAndManagers() {
        try {
            List<EmployeeManager> employees = employeeManagerService.getAllAdminsAndManagers();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch employees: " + e.getMessage());
        }
    }
}


