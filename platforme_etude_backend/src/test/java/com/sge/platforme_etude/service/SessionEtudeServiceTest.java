package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.SessionEtudeDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.SessionEtude;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.StatutSession;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.mapper.SessionEtudeMapper;
import com.sge.platforme_etude.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionEtudeServiceTest {

    @Mock
    private SessionEtudeMapper mapper;

    @Mock
    private SessionEtudeRepo repo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private MatiereRepo matiereRepo;

    @Mock
    private GroupeEtudeRepo groupeEtudeRepo;

    @Mock
    private ObjectifHebdoRepo objectifHebdoRepo;

    @Mock
    private DisponibiliteRepo disponibiliteRepo;

    @InjectMocks
    private SessionEtudeService service;

    @Test
    void createSession_throwsBadRequest_whenMissingUserId() {
        SessionEtudeDto dto = new SessionEtudeDto();
        dto.setMatiereId(2L);
        dto.setDateDebut(LocalDateTime.now());
        dto.setDateFin(LocalDateTime.now().plusMinutes(30));

        assertThrows(BadRequestException.class, () -> service.createSession(dto));
    }

    @Test
    void createSession_throwsBadRequest_whenDurationTooLong() {
        SessionEtudeDto dto = new SessionEtudeDto();
        dto.setUserId(1L);
        dto.setMatiereId(2L);
        dto.setDateDebut(LocalDateTime.of(2026, 5, 12, 8, 0));
        dto.setDateFin(LocalDateTime.of(2026, 5, 12, 12, 30));

        when(userRepo.findById(1L)).thenReturn(Optional.of(new User()));
        when(matiereRepo.findById(2L)).thenReturn(Optional.of(new Matiere()));

        assertThrows(BadRequestException.class, () -> service.createSession(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void createSession_throwsConflict_whenOverlapping() {
        SessionEtudeDto dto = new SessionEtudeDto();
        dto.setUserId(1L);
        dto.setMatiereId(2L);
        dto.setDateDebut(LocalDateTime.of(2026, 5, 12, 10, 30));
        dto.setDateFin(LocalDateTime.of(2026, 5, 12, 11, 30));

        User user = new User();
        user.setId(1L);

        Matiere matiere = new Matiere();
        matiere.setId(2L);

        SessionEtude existing = new SessionEtude();
        existing.setId(99L);
        existing.setUser(user);
        existing.setMatiere(matiere);
        existing.setDateDebut(LocalDateTime.of(2026, 5, 12, 10, 0));
        existing.setDateFin(LocalDateTime.of(2026, 5, 12, 11, 0));
        existing.setStatut(StatutSession.PLANIFIEE);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(matiereRepo.findById(2L)).thenReturn(Optional.of(matiere));
        when(repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> service.createSession(dto));
        verify(repo, never()).save(any());
    }

    @Test
    void createSession_setsDefaults_whenOptionalFieldsMissing() {
        SessionEtudeDto dto = new SessionEtudeDto();
        dto.setUserId(1L);
        dto.setMatiereId(2L);
        dto.setDateDebut(LocalDateTime.of(2026, 5, 12, 9, 0));
        dto.setDateFin(LocalDateTime.of(2026, 5, 12, 10, 0));

        User user = new User();
        user.setId(1L);

        Matiere matiere = new Matiere();
        matiere.setId(2L);
        matiere.setNom("Maths");

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(matiereRepo.findById(2L)).thenReturn(Optional.of(matiere));
        when(repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(mapper.toDto(any(SessionEtude.class))).thenReturn(new SessionEtudeDto());
        when(repo.save(any(SessionEtude.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createSession(dto);

        verify(repo).save(argThat(saved ->
                saved.getTitre().equals("Etude Maths")
                        && saved.getStatut() == StatutSession.PLANIFIEE
                        && saved.getPrivee()
        ));
    }
}

