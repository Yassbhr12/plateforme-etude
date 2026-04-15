package com.sge.platforme_etude.service;


import com.sge.platforme_etude.dto.NotificationDto;
import com.sge.platforme_etude.entite.Notification;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.mapper.NotificationMapper;
import com.sge.platforme_etude.repository.NotificationRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationMapper mapper;
    private final NotificationRepo repo;
    private final UserRepo userRepo;

    public NotificationService(NotificationMapper mapper, NotificationRepo repo, UserRepo userRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional
    public NotificationDto createNotification(NotificationDto dto){
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(()->new RuntimeException("User Not Found"));
        Notification notification = mapper.toEntity(dto,user);
        Notification saved = repo.save(notification);

        return mapper.toDto(saved);
    }

    public NotificationDto findNotificationById(Long id){
        Notification notification = repo.findById(id)
                .orElseThrow(()-> new RuntimeException("Notification Not Found"));

        return mapper.toDto(notification);
    }

    public List<NotificationDto> findAllNotif(){

        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<NotificationDto> findAllNotifByUserId(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        return repo.findNotificationByUser(user)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public NotificationDto findNotificationByIdForUser(Long id, Long userId) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to current user");
        }
        return mapper.toDto(notification);
    }

    @Transactional
    public NotificationDto updateNotificationById(NotificationDto dto , Long id){
        Notification notification = repo.findById(id)
                .orElseThrow(()-> new RuntimeException("Notification Not Found"));

        User user = notification.getUser();
        if (dto.getUserId() != null) {
            user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User Not Found"));
        }

        mapper.updateEntity(notification,dto,user);
        Notification updated = repo.save(notification);

        return mapper.toDto(updated);
    }

    @Transactional
    public NotificationDto updateNotificationByIdForUser(NotificationDto dto, Long id, Long userId) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to current user");
        }
        mapper.updateEntity(notification, dto, notification.getUser());
        return mapper.toDto(repo.save(notification));
    }


    @Transactional
    public NotificationDto readNotification(Long id, Long userId) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to current user");
        }
        notification.setLue(true);
        return mapper.toDto(repo.save(notification));
    }

    @Transactional
    public void deleteNotificationById(Long id){
        Notification notification = repo.findById(id)
                .orElseThrow(()-> new RuntimeException("Notification Not Found"));

        repo.delete(notification);
    }

    @Transactional
    public void deleteNotificationById(Long id, Long userId) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to current user");
        }
        repo.delete(notification);
    }
}
