package com.accesshr.emsbackend.Repo;

import com.accesshr.emsbackend.Entity.EmployeeManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeManagerRepository extends JpaRepository<EmployeeManager, Integer> {
    
    // Custom method to find Employee by their corporate email
    EmployeeManager findByCorporateEmail(String corporateEmail);

    // Find by Employee ID
    EmployeeManager findByEmployeeId(String empId);

    // Check if an employee exists by their Employee ID
    boolean existsByEmployeeId(String employeeId);

    // Find Employees by their Reporting Manager
    @Query("SELECT e FROM EmployeeManager e WHERE e.reportingTo=:reportingTo ORDER BY firstName ASC, lastName ASC")
    List<EmployeeManager> findByReportingTo(@Param ("reportingTo") String reportingTo);

    

    boolean existsByCorporateEmail(String corporateEmail);

    // Find Employees by their Working Country
    @Query("SELECT e FROM EmployeeManager e WHERE e.workingCountry = :workingCountry ORDER BY e.firstName ASC, e.lastName ASC")
    List<EmployeeManager> getEmployeeManagersByCountry(@Param("workingCountry") String workingCountry);
    

    // Custom query to find Employees by their Reporting Manager and Working Country
    @Query("SELECT e FROM EmployeeManager e WHERE e.reportingTo = :employeeId AND e.workingCountry = :workingCountry ORDER BY e.firstName ASC, e.lastName ASC")
    List<EmployeeManager> findReportingTOByWorkingCountry(@Param("employeeId") String employeeId, 
                                                         @Param("workingCountry") String workingCountry);


    @Query("SELECT e FROM EmployeeManager e WHERE e.reportingTo = :employeeId AND e.task = :task ORDER BY e.firstName ASC, e.lastName ASC")
    List<EmployeeManager> findReportingEmployeesForTasks(@Param("employeeId") String employeeId, @Param("task") Boolean task);

    @Query("SELECT e FROM EmployeeManager e ORDER BY e.firstName ASC, e.lastName ASC")
    List<EmployeeManager> findAllEmployeesBYOrder();

    @Query("SELECT e FROM EmployeeManager e WHERE e.role!='employee' ORDER BY e.firstName ASC, e.lastName ASC")
    List<EmployeeManager> getAdminsAndManagers();
    
   
                                                         

    
}
