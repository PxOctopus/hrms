package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalse(User user);
    List<Notification> findByUser(User user);
}
