package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.NotificationDto;
import com.sge.platforme_etude.service.NotificationService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService service;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllNotifByUserId(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/me/notifications/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findNotificationByIdForUser(id, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/me/notifications/{id}")
    public ResponseEntity<NotificationDto> updateNotificationById(@PathVariable Long id, @RequestBody NotificationDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.updateNotificationByIdForUser(dto, id, currentUserService.getCurrentUserId()));
    }

    @PatchMapping("/me/notifications/{id}/read")
    public ResponseEntity<NotificationDto> readNotification(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(service.readNotification(id, currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/me/notifications/{id}")
    public ResponseEntity<String> deleteNotificationById(@PathVariable Long id){
        service.deleteNotificationById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Notification is deleted successfully");
    }
}
