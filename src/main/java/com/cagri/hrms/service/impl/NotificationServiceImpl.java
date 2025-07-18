package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.response.general.NotificationResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.notification.Notification;
import com.cagri.hrms.exception.BusinessException;
import com.cagri.hrms.mapper.NotificationMapper;
import com.cagri.hrms.repository.NotificationRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        List<Notification> notifications = notificationRepository.findByUser(user);
        return notificationMapper.toDtoList(notifications);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void sendLeaveDecisionNotification(Employee employee, boolean approved) {
        String title = "Leave Request " + (approved ? "Approved" : "Rejected");
        String message = "Your leave request has been " + (approved ? "approved" : "rejected") + ".";
        notificationRepository.save(Notification.builder()
                .title(title)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .user(employee.getUser()) // employee related User object
                .build());
    }
}
