package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.general.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDTO> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
}
