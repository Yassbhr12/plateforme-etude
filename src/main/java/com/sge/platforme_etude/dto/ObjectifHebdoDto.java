package com.sge.platforme_etude.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ObjectifHebdoDto {

    private Long id;

    private LocalDate semaine;

    @Min(1)
    @Max(168)
    private Integer heuresCibles;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;

    @Positive
    private Long matiereId;

    private String matiereNom;
}
