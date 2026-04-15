package com.sge.platforme_etude.entite;

import com.sge.platforme_etude.helper.enums.StatutSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class SessionEtude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String titre;

    @NotNull
    private LocalDateTime dateDebut;

    @NotNull
    private LocalDateTime dateFin;

    @NotNull
    @Positive
    @Max(180)
    private Integer dureeMax;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatutSession statut;

    private Boolean privee = true;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;


    @ManyToOne
    @JoinColumn(name = "groupe_etude_id")
    private GroupeEtude groupeEtude;

    @OneToMany(mappedBy = "sessionEtude")
    private List<Commentaire> commentaires;
}
