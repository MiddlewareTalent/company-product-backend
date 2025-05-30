package com.accesshr.emsbackend.EmployeeController.HolidayController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.accesshr.emsbackend.Entity.Holiday;
import com.accesshr.emsbackend.Service.Holiday.HolidayService;
import java.util.List;  // Import List

@RestController
@CrossOrigin
@RequestMapping("/api/holiday")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    // Add a holiday
    @PostMapping(value = "/holiday",produces = "application/json")
    public void addHolidays(@RequestBody Holiday holiday){
        holidayService.addHoliday(holiday);
    }

    // Delete a holiday by ID
    @DeleteMapping(value = "/deleteHolidayById/{id}",produces = "application/json")
    public void deleteHolidayByIds(@PathVariable int id){
        holidayService.deleteHoliday(id);
    }

    // Get all holidays
    @GetMapping(value = "/getAllHolidays",produces = "application/json")
    public List<Holiday> getAllHolidayList() {
        List<Holiday> holidays = holidayService.getAllHolidays();
        return holidays;  // Return the list of holidays
    }
    
    
}
