package com.sge.platforme_etude.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Matiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(unique = true)
    private String nom;

    @NotNull
    @Min(0)
    @Max(5)
    private Integer priorite;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "matiere")
    private List<ObjectifHebdo> objectifsHebdo;

    @OneToMany(mappedBy = "matiere")
    private List<SessionEtude> sessionEtudes;

}
