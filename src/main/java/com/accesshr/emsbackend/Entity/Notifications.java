package com.accesshr.emsbackend.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

//@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
       @Column (name="notificationId")
       int notificationId;

       @Column (name="notificationType")
       String notificationType;

       @Column(name="notification")
       String notification;

       @Column (name="notificationTo")
       String notificationTo;

       @Column (name="isRead")
       Boolean isRead;

   public int getNotificationId() {
      return notificationId;
   }

   public void setNotificationId(int notificationId) {
      this.notificationId = notificationId;
   }

   public String getNotificationType() {
      return notificationType;
   }

   public void setNotificationType(String notificationType) {
      this.notificationType = notificationType;
   }

   public String getNotification() {
      return notification;
   }

   public void setNotification(String notification) {
      this.notification = notification;
   }

   public String getNotificationTo() {
      return notificationTo;
   }

   public void setNotificationTo(String notificationTo) {
      this.notificationTo = notificationTo;
   }

   public Boolean getIsRead(){
      return isRead;
   }

   public void setIsRead(Boolean isRead){
      this.isRead=isRead;
   }

}