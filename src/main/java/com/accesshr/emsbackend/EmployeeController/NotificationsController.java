package com.accesshr.emsbackend.EmployeeController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;
  import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Service.NotificationsService.*;



  @RestController

  @CrossOrigin
  @RequestMapping("apis/employees")

public class NotificationsController {
    @Autowired
    public NotificationsService notificationsService;

     @GetMapping("/notificationsTo/{employeeId}")

    public List<Notifications> getAllNotificationsByEmployee(@PathVariable String employeeId){
      return notificationsService.getAllNotificationsByEmployee(employeeId);
    }


    @PostMapping("/notifications")

    public void addNotifications(@RequestBody Notifications notifications){
       notificationsService.addNotifications(notifications);
    }

    @DeleteMapping("/notificationDelete/{notificationId}")
    public void deleteNotification(@PathVariable int notificationId){
        notificationsService.deleteNotification(notificationId);
    }

    @PutMapping("/notificationUpdate/{notificationId}")
    public void updateNotification(@PathVariable int notificationId, @RequestBody Notifications notifications){
      notificationsService.updateNotification(notificationId, notifications);
    }
}
