package com.accesshr.emsbackend.Service.Holiday;

import java.util.List;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.accesshr.emsbackend.Entity.Holiday;
import com.accesshr.emsbackend.Repo.Holiday.HolidayRepo;
import java.util.*;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepo holidayRepo;

    // Add a holiday
    public Holiday addHoliday(Holiday holiday){
        holidayRepo.save(holiday);
        return holiday;
    }

    // Delete a holiday by ID
    public void deleteHoliday(int id){
        holidayRepo.deleteById(id);  // Use deleteById instead of deleteAllById
    }

    // Get all holidays
    public List<Holiday> getAllHolidays() {
    	 LocalDate currentDate = LocalDate.now();
    	  holidayRepo.deletePreviousHoliday(currentDate);
          System.out.println("The past holidays are deleted");
         return holidayRepo.findAll();  
    }
}
