package com.sge.platforme_etude.dto;


import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentaireDto {

    private Long id;

    @Size(max = 1000)
    private String contenu;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;

    @Positive
    private Long sessionEtudeId;

    private String sessionEtudeTitre;


}
