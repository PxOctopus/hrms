package com.cagri.hrms.dto.request.general;

import lombok.Data;

@Data
public class NotificationRequestDTO {
    private String title;
    private String message;
    private Long userId;
}
