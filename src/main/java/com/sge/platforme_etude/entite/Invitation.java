package com.sge.platforme_etude.entite;

import com.sge.platforme_etude.helper.enums.StatutInvitation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatutInvitation statut;

    @NotNull
    private LocalDateTime dateEnvoi;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "groupe_etude_id")
    private GroupeEtude groupeEtude;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;


}
