package com.sge.platforme_etude.dto.authentification;

import com.sge.platforme_etude.helper.enums.Role;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String refreshToken;   // ← nouveau champ
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private Role role;


    public AuthResponse(String token, String refreshToken,
                        Long id, String email,
                        String nom, String prenom, Role role) {
        this.token = token;
        this.type = "Bearer";
        this.refreshToken = refreshToken;
        this.id = id;
        this.login = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }
}