package com.sge.platforme_etude.dto;


import com.sge.platforme_etude.helper.enums.StatutSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SessionEtudeDto {

    private Long id;

    @Size(max = 200)
    private String titre;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @Positive
    @Max(180)
    private Integer dureeMax;

    private StatutSession statut;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;

    @Positive
    private Long matiereId;

    private String matiereNom;

    @Positive
    private Long groupeEtudeId;

    private String groupeEtudeNom;

    private Boolean privee;


}
