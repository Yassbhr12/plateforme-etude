package com.sge.platforme_etude.dto;


import com.sge.platforme_etude.helper.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class UserDto {

    private Long id;

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenom;

    @Email
    @Size(max = 150)
    private String email;

    private Role role;

    private Boolean actif;



}
