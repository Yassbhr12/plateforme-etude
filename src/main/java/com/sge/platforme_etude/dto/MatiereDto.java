package com.sge.platforme_etude.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatiereDto {

    private Long id;

    @Size(max = 120)
    private String nom;

    @Min(0)
    @Max(5)
    private Integer priorite;

    @Positive
    private Long userId;

    private String userNom;

    private String userEmail;

}
