package com.accesshr.emsbackend.Service.LeaveService;

import com.accesshr.emsbackend.Entity.Holiday;
import com.accesshr.emsbackend.Entity.LeaveRequest;
import com.accesshr.emsbackend.Entity.LeaveSheet;
import com.accesshr.emsbackend.Repo.Holiday.HolidayRepo;
import com.accesshr.emsbackend.Repo.LeaveRepo.LeaveRequestRepo;
import com.accesshr.emsbackend.Repo.LeaveRepo.LeaveSheetRepository;
import com.accesshr.emsbackend.Util.HolidaysUtil;
import com.accesshr.emsbackend.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestServiceImpl.class);

    // Injecting EmailService for sending notifications
    @Autowired
    private EmailService emailService;

    private final HolidayRepo holidayRepo;

    // Injecting LeaveRequestRepo for database operations
    @Autowired
    private LeaveRequestRepo leaveRequestRepository;

    @Autowired
    private LeaveSheetRepository leaveSheetRepository;

    public LeaveRequestServiceImpl(HolidayRepo holidayRepo) {
        this.holidayRepo = holidayRepo;
    }

    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        String employeeId = leaveRequest.getEmployeeId();

        logger.info("Submitting leave request for employeeId={}, from {} to {}",
                employeeId, leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());
        // Check if leave start and end dates are provided
        if (leaveRequest.getLeaveStartDate() == null || leaveRequest.getLeaveEndDate() == null) {
            throw new RuntimeException("Leave start date and end date must be provided.");
        }

        long overlappingCount = leaveRequestRepository.countOverlappingLeaves(leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());
        if (overlappingCount > 0) {
            logger.warn("Overlapping leave found for employeeId={} during {} to {}",
                    employeeId, leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate());
            throw new RuntimeException("You have already applied for leave on one or more of these dates.");
        }
//        Optional<LeaveRequest> existingLeave = leaveRequestRepository.findByEmployeeIdAndLeaveStartDateAndLeaveEndDate(
//                leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()
//        );
        Optional<LeaveRequest> existingLeave = leaveRequestRepository.findActiveLeaveByEmployeeAndStartAndEndDate(
                leaveRequest.getEmployeeId(), leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate()
        );


        if (existingLeave.isPresent()) {
            logger.warn("Duplicate leave found for employeeId={} on same start and end date: {}", employeeId, existingLeave.get().getId());
            throw new RuntimeException("You have already applied for leave on the same start and end date.");
        }

//        if (!leaveRequest.isLOP()) {
//            validateLeaveBalance(leaveRequest);
//        } else {
//            validateLeaveBalance(leaveRequest);
//        }

        // Set status to PENDING and calculate leave duration
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.PENDING);
        int year = leaveRequest.getLeaveStartDate().getYear();
//        List<LocalDate> nationalHolidays = HolidaysUtil.getNationalHolidays(year);
//        leaveRequest.calculateDuration(nationalHolidays);

        List<LocalDate> nationalHolidays = holidayRepo.findByYear(year).stream().map(Holiday::getDate).toList();
        leaveRequest.calculateDuration(nationalHolidays);

        // Save the leave request and send an email notification to the manager
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request submitted successfully for employeeId={}, requestId={}", employeeId, savedRequest.getId());
        emailService.sendLeaveRequestEmail(leaveRequest.getManagerEmail(), leaveRequest);
        return savedRequest;
    }

    public void validateLeaveBalance(LeaveRequest leaveRequest) {
        String employeeId = leaveRequest.getEmployeeId();
        String leaveType = leaveRequest.getLeaveSheet().getLeaveType();
        int leaveYear = leaveRequest.getLeaveStartDate().getYear();
        LeaveSheet leaveSheet = leaveSheetRepository.findByLeaveType(leaveType).orElseThrow(() -> new ResourceNotFoundException("Leave type not configured for leaveType: " + leaveType));
        double maxLeaves = leaveSheet.getNoOfDays();
        List<LocalDate> nationalHolidays = holidayRepo.findByYear(leaveYear)
                .stream()
                .map(Holiday::getDate)
                .toList();
        double requestedDays = leaveRequest.calculateBusinessDays(leaveRequest.getLeaveStartDate(), leaveRequest.getLeaveEndDate(), nationalHolidays);
        double approvedPaid = leaveRequestRepository
                .getApprovedPaidLeaves(employeeId, leaveType).orElse(0.0);

//        double approvedLOP = leaveRequestRepository
//                .getApprovedLOPDays(employeeId, leaveType)
//                .orElse(0.0);
        double remainingPaidLeave = maxLeaves - approvedPaid;

        if (remainingPaidLeave <= 0) {
            throw new RuntimeException("No remaining paid leaves available. Please submit as LOP.");
        }

        logger.info("EmployeeId={}, LeaveType={}, Max={}, ApprovedPaid={}, RemainingPaid={}, Requested={}",
                employeeId, leaveType, maxLeaves, approvedPaid, remainingPaidLeave, requestedDays);
//        if (remainingPaidLeave >= requestedDays) {
//            leaveRequest.setLopDays(0.0);
//            leaveRequest.setLOP(false);
//        } else if (remainingPaidLeave <= 0) {
//            leaveRequest.setLopDays(requestedDays);
//            leaveRequest.setLOP(true);
//        } else {
//            leaveRequest.setLopDays(requestedDays - remainingPaidLeave); // Partial LOP
//        }
        if (requestedDays > remainingPaidLeave) {
            throw new RuntimeException("Requested days exceed remaining paid leave. Please adjust the request.");
        }
        logger.info("Validated leave balance: employeeId={}, leaveType={}, allowed={}, approvedPaid={}, remaining={}, requested={}, lopDays={}",
                employeeId, leaveType, maxLeaves, approvedPaid, remainingPaidLeave, requestedDays, leaveRequest.getLopDays());
    }


    public double getRemainingLeaveDays(String employeeId, String leaveType) {
        LeaveSheet leaveSheet = leaveSheetRepository.findByLeaveType(leaveType).orElseThrow(() -> new RuntimeException("Leave type not found"));
        double maxAllowed = leaveSheet.getNoOfDays();
        double approvedPaid = leaveRequestRepository.getApprovedPaidLeaves(employeeId, leaveType).orElse(0.0);
        return maxAllowed - approvedPaid;
    }


    // Method to approve a leave request
    public LeaveRequest approveLeaveRequest(Long id) {
        logger.info("Approving leave request with id={}", id);
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave Request Id Not Found"));
        if (leaveRequest.getLeaveStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending leave requests can be approved.");
        }
//        validateLeaveBalance(leaveRequest);
        if (!leaveRequest.isLOP()) {
            validateLeaveBalance(leaveRequest);
        }
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.APPROVED);

        if (leaveRequest.isLOP()) {
            double totalLopDays = leaveRequestRepository
                    .getApprovedLOPDays(leaveRequest.getEmployeeId(), leaveRequest.getLeaveSheet().getLeaveType())
                    .orElse(0.0);


            List<LocalDate> nationalHolidays = holidayRepo.findByYear(
                    leaveRequest.getLeaveStartDate().getYear()
            ).stream().map(Holiday::getDate).toList();

            double currentRequestDays = leaveRequest.calculateBusinessDays(
                    leaveRequest.getLeaveStartDate(),
                    leaveRequest.getLeaveEndDate(),
                    nationalHolidays
            );

            leaveRequest.setLopDays(currentRequestDays);

            totalLopDays += currentRequestDays;

            logger.info("Total LOP days after approval for employeeId={}, leaveType={} is {}",
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getLeaveSheet().getLeaveType(),
                    totalLopDays);
        }
        LeaveRequest approvedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request approved for employeeId={}, requestId={}, LOP Days={}",
                approvedRequest.getEmployeeId(), approvedRequest.getId(), approvedRequest.getLopDays());
        emailService.sendResponseToEmployee(approvedRequest.getLeaveStatus(), approvedRequest);
        return approvedRequest;
    }


    // Method to reject a leave request and provide a reason
    @Override
    public LeaveRequest rejectLeaveRequest(Long id, String leaveReason) {
        logger.info("Rejecting leave request with id={}, reason={}", id, leaveReason);
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave Request Id Not Found"));
        leaveRequest.setLeaveStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setLeaveReason(leaveReason);
        leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request rejected for employeeId={}, requestId={}, reason={}",
                leaveRequest.getEmployeeId(), id, leaveReason);
        emailService.sendApprovalNotification(leaveRequest.getLeaveStatus(), leaveRequest);
        return leaveRequest;
    }

    // Method to retrieve a leave request by ID
    public LeaveRequest getLeaveRequestById(Long id) {
        logger.debug("Fetching leave request by id={}", id);
        Optional<LeaveRequest> leaveRequest = leaveRequestRepository.findById(id);
        if (leaveRequest.isEmpty()) {
            throw new ResourceNotFoundException("Leave Request Id Not Found");
        }
        logger.debug("Leave request retrieved for id={}, employeeId={}", id, leaveRequest.get().getEmployeeId());
        return leaveRequest.get();
    }

    public LinkedHashMap<String, Long> getEmpAndLeaveStatus(String employeeId) {
        logger.info("Fetching leave status summary for employeeId={}", employeeId);
        List<LeaveRequest> leaveRequest = leaveRequestRepository.findByEmployeeId(employeeId);
        long leaveCount = leaveRequest.stream().count();
        long pending = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.PENDING).count();
        long approved = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.APPROVED).count();
        long reject = leaveRequest.stream().filter(ele -> ele.getLeaveStatus() == LeaveRequest.LeaveStatus.REJECTED).count();
        logger.debug("Leave summary for employeeId={}: total={}, pending={}, approved={}, reject={}",
                employeeId, leaveCount, pending, approved, reject);
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
        existingLeaveRequest.setMedicalDocument(leaveRequest.getMedicalDocument());
        return leaveRequestRepository.save(existingLeaveRequest);
    }

    // Method to delete an existing leave request
    @Override
    public String deleteLeaveRequest(Long id) {
        logger.info("Attempting to delete leave request with ID={}", id);
        LeaveRequest deleteRequest = leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found for ID: " + id));
        if (deleteRequest.getLeaveStatus() != LeaveRequest.LeaveStatus.PENDING) {
            logger.warn("Leave request with ID={} cannot be deleted as it is not in PENDING status. Current status={}",
                    id, deleteRequest.getLeaveStatus());
            throw new ResourceNotFoundException("Only PENDING leave requests can be deleted.");
        }
        leaveRequestRepository.delete(deleteRequest);
        logger.info("Leave request with ID={} deleted successfully for employeeId={}", id, deleteRequest.getEmployeeId());
        return "Leave request deleted successfully";
    }
}
