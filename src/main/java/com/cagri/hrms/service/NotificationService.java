package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.general.NotificationResponseDTO;
import com.cagri.hrms.entity.employee.Employee;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDTO> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
    /**
     * Sends a notification to the user associated with the given employee
     * regarding the decision on their leave request (approved or rejected).
     */
    void sendLeaveDecisionNotification(Employee employee, boolean approved);
}
