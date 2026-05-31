package com.sge.platforme_etude.dto;

import com.sge.platforme_etude.helper.enums.StatutInvitation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvitationDto {

    private Long id;

    private StatutInvitation statut;

    @PastOrPresent
    private LocalDateTime dateEnvoi;

    @Positive
    private Long groupeEtudeId;

    private String groupeEtudeNom;

    @Positive
    private Long senderId;

    private String senderNom;

    private String senderEmail;

    @Positive
    private Long receiverId;

    private String receiverNom;

    @Email
    @Size(max = 150)
    private String receiverEmail;
}
