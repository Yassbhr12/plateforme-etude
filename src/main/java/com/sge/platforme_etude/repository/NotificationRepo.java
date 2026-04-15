package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.Notification;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.TypeNotif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepo extends JpaRepository<Notification , Long> {
    List<Notification> findNotificationByType(TypeNotif type);

    List<Notification> findNotificationByUser(User user);

    List<Notification> findNotificationByUserId(Long userId);

}
