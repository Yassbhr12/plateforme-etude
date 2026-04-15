package com.sge.platforme_etude.dto.authentification;

import com.sge.platforme_etude.helper.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private Role role;

    public AuthResponse(String token, Long id, @Email @Size(max = 150) String email, @Size(max = 100) String nom, @Size(max = 100) String prenom, Role role) {


    }
}
