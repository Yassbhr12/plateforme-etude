package com.sge.platforme_etude.entite;


import com.sge.platforme_etude.helper.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nom;

    @NotBlank
    @Size(max = 100)
    private String prenom;

    @NotBlank
    @Size(max = 150)
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 8 ,max = 255)
    private String motDePasse;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime dateCreation = LocalDateTime.now();

    private String validationCode;

    private LocalDateTime validationCodeExpiration;

    @NotNull
    private Boolean actif = true;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user" )
    private List<Matiere> matieres;

    @OneToMany(mappedBy = "user")
    private List<ObjectifHebdo> objectifHebdos;

    @OneToMany(mappedBy = "user")
    private List<Disponibilite> disponibilites;

    @OneToMany(mappedBy = "user")
    private List<SessionEtude> sessionEtudes;

    @ManyToMany
    @JoinTable(
            name = "user_groupe_etude",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_etude_id")
    )
    private List<GroupeEtude> groupeEtudes = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private List<Invitation> invitationsEnvoyees;

    @OneToMany(mappedBy = "receiver")
    private List<Invitation> invitationsRecues;

    @OneToMany(mappedBy = "user")
    private List<MessageChat> messagesChat;

    @OneToMany(mappedBy = "user")
    private List<Commentaire> commentaires;

}
