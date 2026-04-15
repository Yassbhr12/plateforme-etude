package com.sge.platforme_etude.entite;

import com.sge.platforme_etude.helper.enums.TypeNotif;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter

public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TypeNotif type;

    @NotBlank
    @Size(max = 500)
    private String message;

    @NotNull
    private LocalDateTime dateEnvoi;

    private boolean lue = false;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
