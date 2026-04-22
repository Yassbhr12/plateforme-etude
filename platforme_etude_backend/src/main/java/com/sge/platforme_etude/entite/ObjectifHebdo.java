package com.sge.platforme_etude.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_objectif_user_matiere_semaine", columnNames = {"user_id", "matiere_id", "semaine"})
        }
)
@Getter
@Setter
public class ObjectifHebdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate semaine;

    @NotNull
    @Min(1)
    @Max(168)
    private Integer heuresCibles;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;
}
