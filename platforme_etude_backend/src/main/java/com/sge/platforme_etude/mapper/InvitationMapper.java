package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.InvitationDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.Invitation;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvitationMapper {

    public InvitationDto toDto(Invitation invitation) {
        if (invitation == null) {
            return null;
        }

        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setStatut(invitation.getStatut());
        dto.setDateEnvoi(invitation.getDateEnvoi());

        if (invitation.getGroupeEtude() != null) {
            dto.setGroupeEtudeId(invitation.getGroupeEtude().getId());
            dto.setGroupeEtudeNom(invitation.getGroupeEtude().getNom());
        }

        if (invitation.getSender() != null) {
            dto.setSenderId(invitation.getSender().getId());
            dto.setSenderNom(invitation.getSender().getNom());
            dto.setSenderEmail(invitation.getSender().getEmail());
        }

        if (invitation.getReceiver() != null) {
            dto.setReceiverId(invitation.getReceiver().getId());
            dto.setReceiverNom(invitation.getReceiver().getNom());
            dto.setReceiverEmail(invitation.getReceiver().getEmail());
        }

        return dto;
    }

    public Invitation toEntity(InvitationDto dto, GroupeEtude groupeEtude, User sender, User receiver) {
        if (dto == null) {
            return null;
        }

        Invitation invitation = new Invitation();
        invitation.setId(dto.getId());
        invitation.setStatut(dto.getStatut());
        invitation.setDateEnvoi(dto.getDateEnvoi());
        invitation.setGroupeEtude(groupeEtude);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);

        return invitation;
    }

    public void updateEntity(Invitation invitation, InvitationDto dto, GroupeEtude groupeEtude, User sender, User receiver) {
        if (invitation == null || dto == null) {
            return;
        }

        invitation.setStatut(dto.getStatut());
        invitation.setDateEnvoi(dto.getDateEnvoi());
        invitation.setGroupeEtude(groupeEtude == null ? invitation.getGroupeEtude() : groupeEtude);
        invitation.setSender(sender == null ? invitation.getSender() : sender);
        invitation.setReceiver(receiver == null ? invitation.getReceiver() : receiver);
    }

    public List<InvitationDto> toDtoList(List<Invitation> invitations) {
        if (invitations == null) {
            return List.of();
        }
        return invitations.stream()
                .map(this::toDto)
                .toList();
    }
}

