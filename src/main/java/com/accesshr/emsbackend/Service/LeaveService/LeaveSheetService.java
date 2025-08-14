package com.accesshr.emsbackend.Service.LeaveService;

import com.accesshr.emsbackend.Entity.LeaveSheet;
import com.accesshr.emsbackend.Repo.LeaveRepo.LeaveSheetRepository;
import com.accesshr.emsbackend.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveSheetService {

    private static final Logger log = LoggerFactory.getLogger(LeaveSheetService.class);

    @Autowired
    private LeaveSheetRepository leaveSheetRepository;

    public List<LeaveSheet> createLeaveSheet(List<LeaveSheet> leaveSheet) {

//        for (LeaveSheet sheet : leaveSheet) {
//            leaveSheetRepository.findByLeaveType(sheet.getLeaveType())
//                    .ifPresent(existing -> {
//                        throw new IllegalArgumentException(
//                                "Leave type '" + sheet.getLeaveType() + "' already exists.");
//                    });
//        }
        return leaveSheetRepository.saveAll(leaveSheet);
    }

    public LeaveSheet updateLeaveSheet(int id, LeaveSheet leaveSheet) {
        LeaveSheet updateSheet = leaveSheetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave sheet is not found"));
        if (updateSheet != null) {
            updateSheet.setLeaveType(leaveSheet.getLeaveType());
            updateSheet.setNoOfDays(leaveSheet.getNoOfDays());
            return leaveSheetRepository.save(updateSheet);
        } else {
            return null;
        }
    }

    public void deleteById(int id) {
        LeaveSheet delete = leaveSheetRepository.findById(id).orElseThrow(() -> new RuntimeException("Leave sheet id not found"));
        leaveSheetRepository.delete(delete);
    }


    public List<LeaveSheet> getAllLeaveSheets() {
        return leaveSheetRepository.findAll();
    }
}
