package com.accesshr.emsbackend.Service.LeaveService;

import com.accesshr.emsbackend.Entity.LeaveRequest;
import com.accesshr.emsbackend.Entity.LeaveSheet;
import com.accesshr.emsbackend.Repo.LeaveRepo.LeaveRequestRepo;
import com.accesshr.emsbackend.Repo.LeaveRepo.LeaveSheetRepository;
import com.accesshr.emsbackend.Util.HolidaysUtil;
import com.accesshr.emsbackend.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    // Injecting EmailService for sending notifications
    @Autowired
    private EmailService emailService;

    // Injecting LeaveRequestRepo for database operations
    @Autowired
    private LeaveRequestRepo leaveRequestRepository;

    @Autowired
    private LeaveSheetRepository leaveSheetRepository;

    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        // Check if leave start and end dates are provided
        if (leaveRequest.getLeaveStartDate() == null || leaveRequest.getLeaveEndDate() == null) {
            throw new RuntimeException("Leave start date and end date must be provided.");
        }

        long overlappingCount = leaveRequestRepository.countOverlappingLeaves(leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());
        if (overlappingCount > 0) {
            throw new RuntimeException("You have already applied for leave on one or more of these dates.");
        }
//        Optional<LeaveRequest> existingLeave = leaveRequestRepository.findByEmployeeIdAndLeaveStartDateAndLeaveEndDate(
//                leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()
//        );
        Optional<LeaveRequest> existingLeave = leaveRequestRepository.findActiveLeaveByEmployeeAndStartAndEndDate(
                leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()
        );


        if (existingLeave.isPresent()) {
            throw new RuntimeException("You have already applied for leave on the same start and end date.");
        }

        if (!leaveRequest.isLOP()) {
            validateLeaveBalance(leaveRequest);
        } else {
            validateLeaveBalance(leaveRequest);
        }

        // Set status to PENDING and calculate leave duration
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.PENDING);
        int year = leaveRequest.getLeaveStartDate().getYear();
        List<LocalDate> nationalHolidays = HolidaysUtil.getNationalHolidays(year);
        leaveRequest.calculateDuration(nationalHolidays);

        // Save the leave request and send an email notification to the manager
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        emailService.sendLeaveRequestEmail(leaveRequest.getManagerEmail(), leaveRequest);
        return savedRequest;
    }


//    // Method to validate if the employee has remaining leave balance for the requested type
//    public void validateLeaveBalance(LeaveRequest leaveRequest) {
////        long leaveCount = leaveRequestRepository.countByEmployeeIdAndLeaveType(leaveRequest.getEmployeeId(), leaveRequest.getLeaveType());
//        Integer totalLeaveDaysTaken = leaveRequestRepository.getTotalLeaveDaysByEmployeeIdAndLeaveType(leaveRequest.getEmployeeId(), leaveRequest.getLeaveType()).orElse(0);
//
//        int currentYear = LocalDate.now().getYear();
//        int leaveYear = leaveRequest.getLeaveStartDate().getYear();
//
//        // Reset the leave balance if the year has changed
//        if (leaveYear < currentYear) {
//            totalLeaveDaysTaken =0;
//        }
//
//        List<LeaveSheet> leaveSheet = leaveSheetRepository.findAll();
//        if (leaveSheet.isEmpty()){
//            throw new ResourceNotFoundException("Leave sheet data is unavailable");
//        }
//
//        // Determine the max leaves based on the leave type
//        int maxLeaves = switch (leaveRequest.getLeaveType()) {
//            case SICK -> leaveSheet.get(0).getSICK();
//            case VACATION -> leaveSheet.get(0).getVACATION();
//            case CASUAL -> leaveSheet.get(0).getCASUAL();
//            case MARRIAGE -> leaveSheet.get(0).getMARRIAGE();
//            case PATERNITY -> leaveSheet.get(0).getPATERNITY();
//            case MATERNITY -> leaveSheet.get(0).getMATERNITY();
//            case OTHERS -> leaveSheet.get(0).getOTHERS();
//            default -> throw new ResourceNotFoundException("Invalid leave type.");
//        };
//
//        double requestedLeaveDays = leaveRequest.calculateBusinessDays(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate(), HolidaysUtil.getNationalHolidays(leaveRequest.getLeaveStartDate().getYear()));
//
//        double remainingLeaveDays = maxLeaves - totalLeaveDaysTaken;
////        if (totalLeaveDaysTaken + requestedLeaveDays > maxLeaves) {
////            throw new ResourceNotFoundException(true,"You have exhausted your " + leaveRequest.getLeaveType().name().toLowerCase() + " leave limit of " + maxLeaves + " days. You have " + remainingLeaveDays + " remaining leave days.");
//////            throw new ResourceNotFoundException("You have exhausted your " + leaveRequest.getLeaveType().name().toLowerCase() + " leave limit of " + maxLeaves + " days. You have " + remainingLeaveDays + " remaining leave days.", String.valueOf(!leaveRequest.isLOP()));
////        }
//        if(requestedLeaveDays>remainingLeaveDays){
//            if(leaveRequest.isLOP()){
//                double lopDays= requestedLeaveDays - remainingLeaveDays;
//                leaveRequest.setLopDays(lopDays);
//            }else{
//                throw new ResourceNotFoundException(true, "You have exhausted your " + leaveRequest.getLeaveType().name().toLowerCase() +
//                        " leave limit of " + maxLeaves + " days. You have " + remainingLeaveDays + " remaining leave days.");
//            }
//        }else{
//            leaveRequest.setLopDays(0.0);
//        }
//    }

    public void validateLeaveBalance(LeaveRequest leaveRequest) {
        int leaveYear = leaveRequest.getLeaveStartDate().getYear();
        List<LeaveSheet> leaveSheet = leaveSheetRepository.findAll();

        if (leaveSheet.isEmpty()) {
            throw new ResourceNotFoundException("Leave sheet data is unavailable");
        }

        int maxLeaves = switch (leaveRequest.getLeaveType()) {
            case SICK -> leaveSheet.get(0).getSICK();
            case VACATION -> leaveSheet.get(0).getVACATION();
            case CASUAL -> leaveSheet.get(0).getCASUAL();
            case MARRIAGE -> leaveSheet.get(0).getMARRIAGE();
            case PATERNITY -> leaveSheet.get(0).getPATERNITY();
            case MATERNITY -> leaveSheet.get(0).getMATERNITY();
            case OTHERS -> leaveSheet.get(0).getOTHERS();
            default -> throw new ResourceNotFoundException("Invalid leave type.");
        };

        double requestedDays = leaveRequest.calculateBusinessDays(
                leaveRequest.getLeaveStartDate(),
                leaveRequest.getLeaveEndDate(),
                HolidaysUtil.getNationalHolidays(leaveYear)
        );

        int approvedPaid = leaveRequestRepository
                .getApprovedPaidLeaveDays(leaveRequest.getEmployeeId(), leaveRequest.getLeaveType())
                .orElse(0);

        int remaining = maxLeaves - approvedPaid;

        // Case 1: Fully within paid leaves
        if (remaining >= requestedDays) {
            leaveRequest.setLopDays(0.0);
        }
        // Case 2: No remaining paid leaves
        else if (remaining <= 0) {
            if (leaveRequest.isLOP()) {
                leaveRequest.setLopDays(requestedDays);  // All days are LOP
            } else {
                throw new ResourceNotFoundException(true, "You have exhausted your " +
                        leaveRequest.getLeaveType().name().toLowerCase() +
                        " leave. You can enable LOP for " + requestedDays + " days.");
            }
        }
        // Case 3: Partially within paid leaves
        else {
            double neededLop = requestedDays - remaining;
            if (leaveRequest.isLOP()) {
                leaveRequest.setLopDays(neededLop);
            } else {
                throw new ResourceNotFoundException(true, "You have only " + remaining + " " +
                        leaveRequest.getLeaveType().name().toLowerCase() +
                        " leave(s) left. Enable LOP for " + neededLop + " day(s).");
            }
        }
    }

    public double getRemainingLeaveDays(String employeeId, LeaveRequest.LeaveType leaveType) {
//        Integer totalLeaveDaysTaken = leaveRequestRepository.getTotalLeaveDaysByEmployeeIdAndLeaveType(employeeId, leaveType).orElse(0);
        Integer approvedPaidLeaveDays = leaveRequestRepository.getApprovedPaidLeaveDays(employeeId, leaveType).orElse(0);
        List<LeaveSheet> leaveSheet = leaveSheetRepository.findAll();
        if (leaveSheet.isEmpty()) {
            throw new ResourceNotFoundException("Leave sheet data is unavailable");
        }
        int maxLeaves = switch (leaveType) {
            case SICK -> leaveSheet.get(0).getSICK();
            case VACATION -> leaveSheet.get(0).getVACATION();
            case CASUAL -> leaveSheet.get(0).getCASUAL();
            case MARRIAGE -> leaveSheet.get(0).getMARRIAGE();
            case PATERNITY -> leaveSheet.get(0).getPATERNITY();
            case MATERNITY -> leaveSheet.get(0).getMATERNITY();
            case OTHERS -> leaveSheet.get(0).getOTHERS();
            default -> throw new ResourceNotFoundException("Invalid leave type.");
        };
        return maxLeaves - approvedPaidLeaveDays;
    }


    private LeaveRequest getLeaveBalance(String employeeId, LeaveRequest.LeaveType leaveType) {
        Optional<LeaveRequest> leaveBalance = leaveRequestRepository.findByEmployeeIdAndLeaveType(employeeId, leaveType);
        return leaveBalance.orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee: " + employeeId));
    }


    // Method to approve a leave request
    @Override
    public LeaveRequest approveLeaveRequest(Long id) {

        // Fetch leave request by ID and update status to APPROVED
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave Request Id Not Found"));
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequestRepository.save(leaveRequest);
        emailService.sendResponseToEmployee(leaveRequest.getLeaveStatus(), leaveRequest);
        return leaveRequest;
    }


    // Method to reject a leave request and provide a reason
    @Override
    public LeaveRequest rejectLeaveRequest(Long id, String leaveReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave Request Id Not Found"));
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setLeaveReason(leaveReason);
        leaveRequestRepository.save(leaveRequest);
        emailService.sendApprovalNotification(leaveRequest.getLeaveStatus(), leaveRequest);
        return leaveRequest;
    }

    // Method to retrieve a leave request by ID
    public LeaveRequest getLeaveRequestById(Long id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(id);
        if (!leaveRequest.isPresent()) {
            throw new ResourceNotFoundException("Leave Request Id Not Found");
        }
        return leaveRequest.get();
    }

    public LinkedHashMap<String, Long> getEmpAndLeaveStatus(String employeeId){
        List<LeaveRequest> leaveRequest = leaveRequestRepository.findByEmployeeId(employeeId);
        long leaveCount = leaveRequest.stream().count();
        long pending = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING).count();
        long approved = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.APPROVED).count();
        long reject = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.REJECTED).count();
        LinkedHashMap<String, Long> empAndLeaveStatus = new LinkedHashMap<>();
        empAndLeaveStatus.put("leaveCount", leaveCount);
        empAndLeaveStatus.put("pending", pending);
        empAndLeaveStatus.put("approved", approved);
        empAndLeaveStatus.put("reject", reject);
        return empAndLeaveStatus;
    }

    // Method to retrieve all leave requests
    @Override
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }


    // Method to retrieve leave requests by manager ID and status
    @Override
    public List<LeaveRequest> getLeaveRequestsByStatus(String managerId, LeaveRequest.LeaveStatus leaveStatus) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByManagerIdAndLeaveStatus(managerId, leaveStatus);
        if (leaveRequests.isEmpty()) {
            throw new ResourceNotFoundException("No " + leaveStatus.name().toLowerCase() + " leave requests found for manager ID: " + managerId);
        }
        return leaveRequests;
    }

    // Method to retrieve leave requests by employee ID and status
    public List<LeaveRequest> getLeaveRequestByEmployeeStatus(String employeeId, LeaveRequest.LeaveStatus leaveStatus) {
        List<LeaveRequest> leaveRequest = leaveRequestRepository.findByEmployeeIdAndLeaveStatus(employeeId, leaveStatus);
        if (leaveRequest.isEmpty()) {
            throw new ResourceNotFoundException("No " + leaveStatus.name().toLowerCase() + " leave requests found for employee ID: " + employeeId);
        }
        return leaveRequest;
    }


    // Method to retrieve all leave requests by manager ID
    @Override
    public List<LeaveRequest> getAllManagerId(String managerId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByManagerId(managerId);
        if (leaveRequests.isEmpty()) {
            throw new ResourceNotFoundException("No leave requests found for manager ID: " + managerId);
        }
        return leaveRequests;
    }


    // Method to retrieve all leave requests by employee ID
    @Override
    public List<LeaveRequest> getAllEmployeeId(String employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findByEmployeeId(employeeId);
        if (leaveRequests.isEmpty()) {
            throw new ResourceNotFoundException("No leave requests found for employeeID: " + employeeId);
        }
        return leaveRequests;
    }

    // Method to update an existing leave request
    @Override
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequest) {
        LeaveRequest existingLeaveRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found for ID: " + id));
        if (existingLeaveRequest.getLeaveStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new ResourceNotFoundException("Only PENDING leave requests can be updated.");
        }
        if (leaveRequest.getLeaveStartDate().isBefore(LocalDate.now()) || leaveRequest.getLeaveEndDate().isBefore(leaveRequest.getLeaveStartDate())) {
            throw new ResourceNotFoundException("Leave start and end dates must be valid and cannot be in the past.");
        }
        Optional<LeaveRequest> existingLeave = leaveRequestRepository.findByEmployeeIdAndLeaveStartDateAndLeaveEndDate(
                leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()
        );
        if (existingLeave.isPresent()) {
            throw new ResourceNotFoundException("You have already applied for leave on the same start and end date.");
        }

        existingLeaveRequest.setLeaveStartDate(leaveRequest.getLeaveStartDate());
        existingLeaveRequest.setLeaveEndDate(leaveRequest.getLeaveEndDate());
        existingLeaveRequest.setLeaveType(leaveRequest.getLeaveType());
        existingLeaveRequest.setMedicalDocument(leaveRequest.getMedicalDocument());
        return leaveRequestRepository.save(existingLeaveRequest);
    }

    // Method to delete an existing leave request
    @Override
    public String deleteLeaveRequest(Long id) {
        LeaveRequest deleteRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found for ID: " + id));
        if (deleteRequest.getLeaveStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new ResourceNotFoundException("Only PENDING leave requests can be deleted.");
        }
        leaveRequestRepository.delete(deleteRequest);
        return "Leave request deleted successfully";
    }


}
