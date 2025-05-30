package com.accesshr.emsbackend.Repo.NotificationsRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
  import com.accesshr.emsbackend.Entity.*;
 import java.util.*;
import java.util.List;

@Repository

public interface NotificationsJpaRepository extends JpaRepository <Notifications, Integer>{
    List<Notifications> getAllByNotificationTo(String employeeId);
}
