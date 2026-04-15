package com.sge.platforme_etude.dto;

import com.sge.platforme_etude.helper.enums.TypeNotif;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {

    private Long id;

    private TypeNotif type;

    @Size(max = 500)
    private String message;

    @PastOrPresent
    private LocalDateTime dateEnvoi;

    private boolean lue;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;
}
