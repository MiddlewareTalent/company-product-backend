package com.accesshr.emsbackend.Service.NotificationsService;

import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
  import org.springframework.web.server.ResponseStatusException;
  import java.util.*;
  import com.accesshr.emsbackend.Entity.*;
  import com.accesshr.emsbackend.Repo.NotificationsRepository.*;

@Service

public class NotificationsService implements NotificationsRepository {
    @Autowired 
    public NotificationsJpaRepository notificationsRepository;

    @Override 
    public void addNotifications(Notifications notifications){
    notificationsRepository.save(notifications);
    }

    @Override
    public List<Notifications> getAllNotificationsByEmployee(String employeeId){
       return notificationsRepository.getAllByNotificationTo(employeeId);
    }

    @Override
    public void deleteNotification(int notificationId){
        notificationsRepository.deleteById(notificationId);
    }

    @Override
    public void updateNotification(int notificationId, Notifications notifications){
      try{
        Notifications newNotifications=notificationsRepository.findById(notificationId).get();
        if(notifications.getNotificationType()!=null){
          newNotifications.setNotificationType(notifications.getNotificationType());
        }
        if(notifications.getNotification()!=null){
          newNotifications.setNotification(notifications.getNotification());
        }
        if(notifications.getNotificationTo()!=null){
          newNotifications.setNotificationTo(notifications.getNotificationTo());
        }
        if(notifications.getIsRead()!=null){
          newNotifications.setIsRead(notifications.getIsRead());
        }
        notificationsRepository.save(newNotifications);
      }
      catch (Exception e){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
    }
}
