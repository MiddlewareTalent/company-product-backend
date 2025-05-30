package com.accesshr.emsbackend.Service;

import java.util.List;

import com.accesshr.emsbackend.Dto.EmployeeManagerDTO;
import com.accesshr.emsbackend.Dto.LoginDTO;
import com.accesshr.emsbackend.Entity.EmployeeManager;
import com.accesshr.emsbackend.response.LoginResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeManagerService {
//    EmployeeManagerDTO addEmployee(EmployeeManagerDTO employeeManagerDTO);
//    EmployeeManagerDTO addAdmin(EmployeeManagerDTO employeeManagerDTO);
//    LoginResponse loginEmployee(LoginDTO loginDTO);
//    List<EmployeeManagerDTO> getAllEmployees(); // New method to fetch all employees
//    EmployeeManagerDTO getEmployeeById(String employeeId); // New method to fetch employee by ID
//    boolean deleteEmployeeById(int employeeId); // New method to delete employee by ID

    EmployeeManagerDTO addEmployee(EmployeeManagerDTO employeeManagerDTO);
    EmployeeManagerDTO addAdmin(String comapny, EmployeeManagerDTO employeeManagerDTO);
    LoginResponse loginEmployee(LoginDTO loginDTO);
    List<EmployeeManagerDTO> getAllEmployees(); // New method to fetch all employees
    //    EmployeeManagerDTO getById(int id); // New method to fetch employee by ID
    boolean deleteById(int id); // New method to delete employee by ID
    EmployeeManagerDTO getEmployeeDataById(String employeeId); // New method to fetch employee by ID

    EmployeeManagerDTO getById(int id);

    EmployeeManagerDTO updateEmployee(int id, EmployeeManagerDTO employeeManagerDTO);
    EmployeeManager getEmployeeById(String employeeId);
    List<EmployeeManager> findOrigin(String employeeId) throws Exception;

    boolean isEmployeeIdPresent(String employeeId);///updated now
	List<EmployeeManager> reportingToList(String empId,String workingCountry);
    List<EmployeeManager> alsoWorkingWith(String empId, String workingCountry ) throws Exception;
    // List<EmployeeManager> alsoWorkingWith(String empId ) throws Exception;
    EmployeeManager changePassword(String employeeId, String password) ;
    boolean changePassword(String email, String oldPassword, String newPassword);
    List <EmployeeManager> getEmployeesByWorkingCountry(String workingCountry);
    boolean existsByCorporateEmail(String corporateEmail);

    List <EmployeeManager> getReportingEmployeesForTasks(String employeeId);

    List <EmployeeManager> getAllEmployeesByOrder();

    List <EmployeeManager> getAllAdminsAndManagers();

    List<EmployeeManager> getAllMyColleagues(String managerId);//

    EmployeeManager changeProfilePhoto(String employeeId,EmployeeManager employee);
}

