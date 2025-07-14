package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.response.general.NotificationResponseDTO;
import com.cagri.hrms.entity.notification.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponseDTO toDto(Notification notification);
    List<NotificationResponseDTO> toDtoList(List<Notification> notifications);
}
