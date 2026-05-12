package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.InvitationDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.Invitation;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.StatutInvitation;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.mapper.InvitationMapper;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.InvitationRepo;
import com.sge.platforme_etude.repository.NotificationRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationMapper mapper;

    @Mock
    private InvitationRepo repo;

    @Mock
    private GroupeEtudeRepo groupeEtudeRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private InvitationService service;

    @Test
    void createInvitation_throwsBadRequest_whenSenderMissing() {
        InvitationDto dto = new InvitationDto();
        dto.setGroupeEtudeId(1L);
        dto.setReceiverId(2L);

        assertThrows(BadRequestException.class, () -> service.createInvitation(dto));
    }

    @Test
    void createInvitation_throwsBadRequest_whenInvitingSelf() {
        InvitationDto dto = new InvitationDto();
        dto.setGroupeEtudeId(1L);
        dto.setSenderId(5L);
        dto.setReceiverId(5L);

        GroupeEtude groupe = new GroupeEtude();
        groupe.setId(1L);

        User sender = new User();
        sender.setId(5L);

        when(groupeEtudeRepo.findById(1L)).thenReturn(Optional.of(groupe));
        when(userRepo.findById(5L)).thenReturn(Optional.of(sender));

        assertThrows(BadRequestException.class, () -> service.createInvitation(dto));
    }

    @Test
    void accepterInvitation_addsMember_andUpdatesStatus() {
        User receiver = new User();
        receiver.setId(20L);

        User sender = new User();
        sender.setId(10L);

        GroupeEtude groupe = new GroupeEtude();
        groupe.setId(7L);
        groupe.setUsers(new ArrayList<>());

        Invitation invitation = new Invitation();
        invitation.setId(1L);
        invitation.setReceiver(receiver);
        invitation.setSender(sender);
        invitation.setGroupeEtude(groupe);
        invitation.setStatut(StatutInvitation.EN_ATTENTE);

        when(repo.findById(1L)).thenReturn(Optional.of(invitation));
        when(repo.save(invitation)).thenReturn(invitation);
        when(groupeEtudeRepo.save(groupe)).thenReturn(groupe);
        when(mapper.toDto(invitation)).thenReturn(new InvitationDto());

        service.accepterInvitation(1L, 20L);

        assertEquals(StatutInvitation.ACCEPTEE, invitation.getStatut());
        assertEquals(1, groupe.getUsers().size());
        assertEquals(20L, groupe.getUsers().get(0).getId());
        verify(groupeEtudeRepo).save(groupe);
        verify(repo).save(invitation);
    }
}

