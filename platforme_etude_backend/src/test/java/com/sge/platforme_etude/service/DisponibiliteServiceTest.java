package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.DisponibiliteDto;
import com.sge.platforme_etude.entite.Disponibilite;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.mapper.DisponibiliteMapper;
import com.sge.platforme_etude.repository.DisponibiliteRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibiliteServiceTest {

    @Mock
    private DisponibiliteMapper mapper;

    @Mock
    private DisponibiliteRepo repo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private DisponibiliteService service;

    @Test
    void createDispo_throwsBadRequest_whenStartAfterEnd() {
        DisponibiliteDto dto = new DisponibiliteDto();
        dto.setJourSemaine(1);
        dto.setHeureDebut(LocalTime.of(12, 0));
        dto.setHeureFin(LocalTime.of(10, 0));

        when(userRepo.findById(1L)).thenReturn(Optional.of(new User()));

        assertThrows(BadRequestException.class, () -> service.createDispo(dto, 1L));
        verify(repo, never()).save(any());
    }

    @Test
    void createDispo_throwsConflict_whenOverlappingSlotExists() {
        User user = new User();
        user.setId(1L);

        DisponibiliteDto dto = new DisponibiliteDto();
        dto.setJourSemaine(2);
        dto.setHeureDebut(LocalTime.of(10, 0));
        dto.setHeureFin(LocalTime.of(12, 0));

        Disponibilite existing = new Disponibilite();
        existing.setId(99L);
        existing.setJourSemaine(2);
        existing.setHeureDebut(LocalTime.of(9, 0));
        existing.setHeureFin(LocalTime.of(11, 0));
        existing.setUser(user);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(repo.findDisponibiliteByUserId(1L)).thenReturn(List.of(existing));
        when(mapper.toEntity(dto, user)).thenReturn(new Disponibilite());

        assertThrows(ConflictException.class, () -> service.createDispo(dto, 1L));
        verify(repo, never()).save(any());
    }

    @Test
    void updateDispoById_throwsNotFound_whenDispoMissing() {
        when(repo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.updateDispoById(new DisponibiliteDto(), 10L, 1L));
    }
}

