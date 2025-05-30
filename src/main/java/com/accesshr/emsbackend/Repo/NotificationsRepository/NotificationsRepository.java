package com.accesshr.emsbackend.Repo.NotificationsRepository;

import com.accesshr.emsbackend.Entity.*;
import java.util.*;

import javax.management.Notification;

public interface NotificationsRepository {
void addNotifications(Notifications notifications);
List<Notifications> getAllNotificationsByEmployee(String employeeId);
void deleteNotification(int id);
void updateNotification(int notificationId, Notifications notifications);
}
