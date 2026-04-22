package com.sge.platforme_etude.dto;


import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageChatDto {

    private Long id;

    @Size(max = 2000)
    private String contenu;

    @PastOrPresent
    private LocalDateTime dateEnvoi;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;

    @Positive
    private Long groupeEtudeId;

    private String groupeEtudeNom;
}
