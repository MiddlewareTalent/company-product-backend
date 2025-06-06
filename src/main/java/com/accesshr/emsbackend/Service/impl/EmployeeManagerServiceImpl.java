package com.accesshr.emsbackend.Service.impl;

import com.accesshr.emsbackend.Dto.EmployeeManagerDTO;
import com.accesshr.emsbackend.Dto.LoginDTO;
import com.accesshr.emsbackend.Entity.EmployeeManager;
import com.accesshr.emsbackend.Repo.EmployeeManagerRepository;
import com.accesshr.emsbackend.Service.EmployeeManagerService;
import com.accesshr.emsbackend.Service.JWT.JWTService;
import com.accesshr.emsbackend.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeManagerServiceImpl implements EmployeeManagerService {

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender emailSender;


    @Override
    public EmployeeManagerDTO addEmployee(EmployeeManagerDTO employeeManagerDTO) {
        return saveEmployee(employeeManagerDTO);
    }

    @Transactional
    public EmployeeManagerDTO addAdmin(String company, EmployeeManagerDTO employeeManagerDTO) {
        // Set the role to Admin
        employeeManagerDTO.setRole("admin");
        EmployeeManagerDTO empDto=saveEmployee(employeeManagerDTO);
         String text="Dear " + employeeManagerDTO.getFirstName() + " " + employeeManagerDTO.getLastName() +
                 ",\nPlease open this link: https://company-product-frontend.azurewebsites.net/" + company + "/login";

      SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(employeeManagerDTO.getEmail());
        message.setSubject("Talentflow registration");
        message.setText(text);
        emailSender.send(message);

        return empDto;
    }

    private EmployeeManagerDTO saveEmployee(EmployeeManagerDTO employeeManagerDTO) {
        EmployeeManager employee = new EmployeeManager();
        employee.setFirstName(employeeManagerDTO.getFirstName());
        employee.setLastName(employeeManagerDTO.getLastName());
        employee.setEmail(employeeManagerDTO.getEmail());
        employee.setCountry(employeeManagerDTO.getCountry());
        employee.setStreetAddress(employeeManagerDTO.getStreetAddress());
        employee.setCity(employeeManagerDTO.getCity());
        employee.setRegion(employeeManagerDTO.getRegion());
        employee.setPostalCode(employeeManagerDTO.getPostalCode());
        employee.setWorkingCountry(employeeManagerDTO.getWorkingCountry());
        employee.setNationalInsuranceNumber(employeeManagerDTO.getNationalInsuranceNumber());
        employee.setEmployeeId(employeeManagerDTO.getEmployeeId());
        employee.setCorporateEmail(employeeManagerDTO.getCorporateEmail());
        employee.setJobRole(employeeManagerDTO.getJobRole());
        employee.setDateOfJoining(employeeManagerDTO.getDateOfJoining());
        employee.setDateOfBirth(employeeManagerDTO.getDateOfBirth());
        employee.setTask(employeeManagerDTO.isTask());
        employee.setOrganizationChart(employeeManagerDTO.isOrganizationChart());
        employee.setTimeSheet(employeeManagerDTO.isTimeSheet());
        employee.setLeaveManagement(employeeManagerDTO.isLeaveManagement());
        employee.setEmploymentStatus(employeeManagerDTO.getEmploymentStatus());
        employee.setReportingTo(employeeManagerDTO.getReportingTo());
        employee.setRole(employeeManagerDTO.getRole());
        employee.setIdentityCard(employeeManagerDTO.getIdentityCard());
        employee.setVisa(employeeManagerDTO.getVisa());
        employee.setOtherDocuments(employeeManagerDTO.getOtherDocuments());
        employee.setProfilePhoto(employeeManagerDTO.getProfilePhoto());
        // Generate or use the provided password
        String password = employeeManagerDTO.getPassword() != null ? employeeManagerDTO.getPassword()
                : UUID.randomUUID().toString().substring(0, 8); // Generate random password if not provided
        String hashedPassword = passwordEncoder.encode(password);
        employee.setPassword(hashedPassword); // Store hashed password

        employeeManagerRepository.save(employee);

        // Set the plain text password back to DTO for display purposes
        employeeManagerDTO.setPassword(password);

        return employeeManagerDTO;
    }

//    @Override
//    public LoginResponse loginEmployee(LoginDTO loginDTO) {
//        EmployeeManager employee = employeeManagerRepository.findByCorporateEmail(loginDTO.getEmail());
//
//        if (employee != null) {
//            boolean isPasswordValid = new BCryptPasswordEncoder().matches(loginDTO.getPassword(), employee.getPassword());
//            if (isPasswordValid) {
//                // Return role along with message and status
//                return new LoginResponse("Login Success", true, employee.getRole());
//            } else {
//                return new LoginResponse("Password does not match", false, null); // No role if login fails
//            }
//        } else {
//            return new LoginResponse("Email does not exist", false, null); // No role if email not found
//        }
//    }

    @Override
    public LoginResponse loginEmployee(LoginDTO loginDTO) {
        EmployeeManager employee = employeeManagerRepository.findByCorporateEmail(loginDTO.getEmail());
        if (employee != null) {
            boolean isPasswordValid = new BCryptPasswordEncoder().matches(loginDTO.getPassword(), employee.getPassword());
            if (isPasswordValid) {
                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
                if (authentication.isAuthenticated()) {
                    String token = jwtService.generateToken(loginDTO.getEmail());
                    return new LoginResponse("Login Success", true, employee.getRole(), token, employee.getFirstName(), employee.getLastName(), employee.getEmployeeId());
                } else {
                    return new LoginResponse("Authentication failed", false, null, null);
                }
            } else {
                return new LoginResponse("Password does not match", false, null, null);
            }
        } else {
            return new LoginResponse("Email not found", false, null, null);
        }
    }

    public boolean existsByCorporateEmail(String corporateEmail) {
        return employeeManagerRepository.existsByCorporateEmail(corporateEmail);
    }

    @Override
    public List<EmployeeManagerDTO> getAllEmployees() {
        List<EmployeeManager> employees = employeeManagerRepository.findAll();
        return employees.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override

    public List<EmployeeManager> getEmployeesByWorkingCountry(String workingCountry) {
        return employeeManagerRepository.getEmployeeManagersByCountry(workingCountry);
    }

    @Override
    public EmployeeManagerDTO getEmployeeDataById(String employeeId) {
        EmployeeManager employee = employeeManagerRepository.findByEmployeeId(employeeId);
        System.out.println(employee);
        if (employee != null) {
            return convertToDTO(employee);
        }
        return null;
    }

    @Override
    public EmployeeManagerDTO getById(int id) {
        Optional<EmployeeManager> employeeId = employeeManagerRepository.findById(id);
        if (employeeId.isEmpty()) {
            return null;
        } else {
            return convertToDTO(employeeId.get());
        }
    }

    @Override
    public EmployeeManagerDTO updateEmployee(int id, EmployeeManagerDTO employeeManagerDTO) {
        EmployeeManager update = employeeManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        if (update != null) {
            update.setFirstName(employeeManagerDTO.getFirstName());
            update.setLastName(employeeManagerDTO.getLastName());
            update.setEmail(employeeManagerDTO.getEmail());
            update.setCountry(employeeManagerDTO.getCountry());
            update.setStreetAddress(employeeManagerDTO.getStreetAddress());
            update.setCity(employeeManagerDTO.getCity());
            update.setRegion(employeeManagerDTO.getRegion());
            update.setPostalCode(employeeManagerDTO.getPostalCode());
            update.setEmployeeId(employeeManagerDTO.getEmployeeId());
            update.setNationalInsuranceNumber(employeeManagerDTO.getNationalInsuranceNumber());
            update.setWorkingCountry(employeeManagerDTO.getWorkingCountry());
            update.setCorporateEmail(employeeManagerDTO.getCorporateEmail());
            update.setJobRole(employeeManagerDTO.getJobRole());
            update.setDateOfJoining(employeeManagerDTO.getDateOfJoining());
            update.setDateOfBirth(employeeManagerDTO.getDateOfBirth());
            update.setTask(employeeManagerDTO.isTask());
            update.setOrganizationChart(employeeManagerDTO.isOrganizationChart());
            update.setTimeSheet(employeeManagerDTO.isTimeSheet());
            update.setLeaveManagement(employeeManagerDTO.isLeaveManagement());
            update.setEmploymentStatus(employeeManagerDTO.getEmploymentStatus());
            update.setReportingTo(employeeManagerDTO.getReportingTo());
            update.setRole(employeeManagerDTO.getRole());
            update.setVisa(employeeManagerDTO.getVisa());
            update.setIdentityCard(employeeManagerDTO.getIdentityCard());
            update.setOtherDocuments(employeeManagerDTO.getOtherDocuments());
            update.setProfilePhoto(employeeManagerDTO.getProfilePhoto());
            employeeManagerRepository.save(update);
        }
        return null;
    }

    @Override
    public boolean deleteById(int id) {
        if (employeeManagerRepository.existsById(id)) {
            employeeManagerRepository.deleteById(id);
            return true;
        }
        return false;
    }

//    @Override
//    public EmployeeManagerDTO getEmployeeById(String employeeId) {
//        EmployeeManager employee = employeeManagerRepository.findById(Integer.valueOf(employeeId)).orElse(null);
//        if (employee != null) {
//            return convertToDTO(employee);
//        }
//        return null;
//    }
//
//    @Override
//    public boolean deleteEmployeeById(int employeeId) {
//        if (employeeManagerRepository.existsById(employeeId)) {
//            employeeManagerRepository.deleteById(employeeId);
//            return true;
//        }
//        return false;
//    }

    // Helper method to convert entity to DTO
    private EmployeeManagerDTO convertToDTO(EmployeeManager employee) {
        EmployeeManagerDTO dto = new EmployeeManagerDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setCountry(employee.getCountry());
        dto.setStreetAddress(employee.getStreetAddress());
        dto.setCity(employee.getCity());
        dto.setRegion(employee.getRegion());
        dto.setPostalCode(employee.getPostalCode());
        dto.setNationalInsuranceNumber(employee.getNationalInsuranceNumber());
        dto.setWorkingCountry(employee.getWorkingCountry());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setCorporateEmail(employee.getCorporateEmail());
        dto.setJobRole(employee.getJobRole());
        dto.setDateOfJoining(employee.getDateOfJoining());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setTask(employee.isTask());
        dto.setOrganizationChart(employee.isOrganizationChart());
        dto.setTimeSheet(employee.isTimeSheet());
        dto.setLeaveManagement(employee.isLeaveManagement());
        dto.setEmploymentStatus(employee.getEmploymentStatus());
        dto.setReportingTo(employee.getReportingTo());
        dto.setRole(employee.getRole());
        dto.setIdentityCard(employee.getIdentityCard());
        dto.setVisa(employee.getVisa());
        dto.setOtherDocuments(employee.getOtherDocuments());
        dto.setProfilePhoto(employee.getProfilePhoto());
        return dto;
    }

    @Override
    /// updated now
    public boolean isEmployeeIdPresent(String employeeId) {
        return employeeManagerRepository.existsByEmployeeId(employeeId);
    }

    // Method to find the origin employee by their empId
    public List<EmployeeManager> findOrigin(String empId) throws Exception {
        System.out.println("Employee Id: " + empId);
        List<EmployeeManager> reportingEmployees = new ArrayList<>();

        // Fetch the initial employee
        EmployeeManager emp = employeeManagerRepository.findByEmployeeId(empId);

        // Check if the employee exists
        if (emp == null) {
            throw new Exception("Employee not found with ID: " + empId);
        }

        reportingEmployees.add(emp);

        String reportingEmpId = emp.getReportingTo();// Assuming this returns the empId of the superior

        // Loop until there are no more employees to fetch
        while (reportingEmpId != null) {
            EmployeeManager superior = employeeManagerRepository.findByEmployeeId(reportingEmpId);

            // If superior is found, add it to the list
            if (superior != null) {
                reportingEmployees.add(superior);
                reportingEmpId = superior.getReportingTo(); // Update reportingEmpId to the next manager
            } else {
                break; // Exit the loop if no more employees are found
            }
        }

        return reportingEmployees; // Return the list of employees
    }

    public List<EmployeeManager> reportingToList(String empId, String workingCountry) {
        EmployeeManager emp = employeeManagerRepository.findByEmployeeId(empId);
        if (emp == null) {
            return Collections.emptyList(); // Return empty list if employee does not exist
        }
        System.out.println(workingCountry);
        if (workingCountry.equals("all")) {
            return employeeManagerRepository.findByReportingTo(empId);
        } else {

            return employeeManagerRepository.findReportingTOByWorkingCountry(empId, workingCountry);

        }
    }


    public EmployeeManager getEmployeeById(String empId) {
        return employeeManagerRepository.findByEmployeeId(empId);

    }

    public List<EmployeeManager> alsoWorkingWith(String empId, String workingCountry) throws Exception {

        List<EmployeeManager> emp1 = employeeManagerRepository.findByReportingTo(empId);
        List<EmployeeManager> emp2 = findOrigin(empId);
        List<EmployeeManager> emp3 = employeeManagerRepository.findAll();

        // Merge emp1 and emp2 lists
        List<EmployeeManager> mergedEmpList = new ArrayList<>(emp1);
        mergedEmpList.addAll(emp2);
        List<EmployeeManager> emp4;
        System.out.println(workingCountry);

        if (workingCountry.equals("all")) {

            emp4 = emp3.stream()
                    .filter(emp -> emp.getEmployeeId() != null)
                    // Filter out employees in the merged list (emp1 + emp2)
                    .filter(emp -> mergedEmpList.stream().noneMatch(e -> e.getEmployeeId() != null && e.getEmployeeId().equals(emp.getEmployeeId())))

                    .collect(Collectors.toList());
        } else {
            emp4 = emp3.stream()
                    .filter(emp -> emp.getEmployeeId() != null)
                    // Filter out employees in the merged list (emp1 + emp2)
                    .filter(emp -> mergedEmpList.stream().noneMatch(e -> e.getEmployeeId() != null && e.getEmployeeId().equals(emp.getEmployeeId())))
                    .filter(emp -> emp.getWorkingCountry().toLowerCase().equals(workingCountry.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return emp4.size() <= 16 ? emp4 : emp4.subList(0, 16);
    }

    public EmployeeManager changePassword(String employeeId, String newPassword) {

        EmployeeManager employeeManager = employeeManagerRepository.findByEmployeeId(employeeId);

        if (employeeManager == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        employeeManager.setPassword(encodedPassword);
        employeeManagerRepository.save(employeeManager);
        return employeeManager;
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        EmployeeManager employeeManager = employeeManagerRepository.findByCorporateEmail(email);
        if (employeeManager == null) {
            throw new RuntimeException("Employee not found with email: " + email);
        }
        if (!passwordEncoder.matches(oldPassword, employeeManager.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        employeeManager.setPassword(passwordEncoder.encode(newPassword));
        employeeManagerRepository.save(employeeManager);
        return true;
    }

    @Override

    public List<EmployeeManager> getReportingEmployeesForTasks(String employeeId) {
        return employeeManagerRepository.findReportingEmployeesForTasks(employeeId, true);
    }

    @Override
    public List<EmployeeManager> getAllEmployeesByOrder() {
        return employeeManagerRepository.findAllEmployeesBYOrder();
    }

    @Override
    public List<EmployeeManager> getAllAdminsAndManagers() {
        return employeeManagerRepository.getAdminsAndManagers();
    }

    @Override
    public List<EmployeeManager> getAllMyColleagues(String managerId) {
        EmployeeManager emp = employeeManagerRepository.findByEmployeeId(managerId);

        if (emp == null) {
            return Collections.emptyList(); // Return empty list if employee does not exist
        } else {
            return employeeManagerRepository.findByReportingTo(managerId);//
        }
    }

    @Override
    public EmployeeManager changeProfilePhoto(String employeeId, EmployeeManager employee) {
       return employeeManagerRepository.save(employee);

    }


}
