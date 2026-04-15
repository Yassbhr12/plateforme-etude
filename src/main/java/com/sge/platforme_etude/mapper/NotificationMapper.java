package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.NotificationDto;
import com.sge.platforme_etude.entite.Notification;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setDateEnvoi(notification.getDateEnvoi());
        dto.setLue(notification.isLue());

        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
            dto.setUserNom(notification.getUser().getNom());
            dto.setUserEmail(notification.getUser().getEmail());
        }

        return dto;
    }

    public Notification toEntity(NotificationDto dto, User user) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setType(dto.getType());
        notification.setMessage(dto.getMessage());
        notification.setDateEnvoi(dto.getDateEnvoi());
        notification.setLue(dto.isLue());
        notification.setUser(user);

        return notification;
    }

    public void updateEntity(Notification notification, NotificationDto dto, User user) {
        if (notification == null || dto == null) {
            return;
        }

        notification.setType(dto.getType());
        notification.setMessage(dto.getMessage());
        notification.setDateEnvoi(dto.getDateEnvoi());
        notification.setLue(dto.isLue());
        notification.setUser(user == null ? notification.getUser() : user);
    }

    public List<NotificationDto> toDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return List.of();
        }
        return notifications.stream()
                .map(this::toDto)
                .toList();
    }
}

