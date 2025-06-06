package com.accesshr.emsbackend.EmployeeController.JobRolesController;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;
  import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Service.JobRolesService.JobRolesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController

  @CrossOrigin
  @RequestMapping("apis/employees/jobRoles")
public class JobRolesController {
    @Autowired 
    public JobRolesService jobRolesService;

    @GetMapping(value="/jobRoles",produces = "application/json")
    public List<JobRoles> getAllJobRoles() {
        return jobRolesService.getAllJobRoles();
    }

    @PostMapping(value="/addJobRole",produces = "application/json")
    public JobRoles addJobRoles(@RequestBody JobRoles jobRoles) {
        
        return jobRolesService.addJobRoles(jobRoles);
    }

    @PutMapping(value="updateJobRole/{jobRoleId}",produces = "application/json")
    public JobRoles updateJobRoles(@PathVariable int jobRoleId, @RequestBody JobRoles jobRoles) {
        
        return jobRolesService.updateJobRoles(jobRoleId, jobRoles);
    }

    @GetMapping(value = "/getJobRoleById/{jobRoleId}", produces = "application/json")
    public JobRoles getJobRolesByJobRoleId(@PathVariable int jobRoleId) {
        return jobRolesService.getJobRoleByJobRoleId(jobRoleId);
    }

    @DeleteMapping(value="/deleteJobRoleById/{jobRoleId}", produces = "application/json")
    public void deleteJobRoles(@PathVariable int jobRoleId){
         jobRolesService.deleteJobRoles(jobRoleId);
    }
    
    
    


}
