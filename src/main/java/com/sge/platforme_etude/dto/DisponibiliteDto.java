package com.sge.platforme_etude.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;


@Getter
@Setter
public class DisponibiliteDto {

    private Long id;

    @Min(1)
    @Max(7)
    private Integer jourSemaine;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;
}
