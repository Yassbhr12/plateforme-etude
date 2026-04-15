package com.sge.platforme_etude.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GroupeEtudeDto {

    private Long id;

    @Size(max = 120)
    private String nom;

    @Size(max = 1000)
    private String description;

    @Positive
    private Long adminId;

    private String adminNom;

    private String adminEmail;

    @Valid
    private List<UserDto> users;


}
