package com.sge.platforme_etude.entite;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class GroupeEtude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(unique = true)
    private String nom ;

    @Size(max = 1000)
    private String description;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "groupeEtude")
    private List<SessionEtude> sessionEtudes;

    @ManyToMany(mappedBy = "groupeEtudes")
    private List<User> users;

    @OneToMany(mappedBy = "groupeEtude")
    private List<Invitation> invitations;

    @OneToMany(mappedBy = "groupeEtude")
    private List<MessageChat> messagesChat;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
}
