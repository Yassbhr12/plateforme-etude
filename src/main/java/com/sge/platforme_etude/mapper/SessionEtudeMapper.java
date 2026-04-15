package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.SessionEtudeDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.SessionEtude;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionEtudeMapper {

    public SessionEtudeDto toDto(SessionEtude sessionEtude) {
        if (sessionEtude == null) {
            return null;
        }

        SessionEtudeDto dto = new SessionEtudeDto();
        dto.setId(sessionEtude.getId());
        dto.setTitre(sessionEtude.getTitre());
        dto.setDateDebut(sessionEtude.getDateDebut());
        dto.setDateFin(sessionEtude.getDateFin());
        dto.setDureeMax(sessionEtude.getDureeMax());
        dto.setStatut(sessionEtude.getStatut());
        dto.setPrivee(sessionEtude.getPrivee());

        if (sessionEtude.getUser() != null) {
            dto.setUserId(sessionEtude.getUser().getId());
            dto.setUserNom(sessionEtude.getUser().getNom());
            dto.setUserEmail(sessionEtude.getUser().getEmail());
        }

        if (sessionEtude.getMatiere() != null) {
            dto.setMatiereId(sessionEtude.getMatiere().getId());
            dto.setMatiereNom(sessionEtude.getMatiere().getNom());
        }

        if (sessionEtude.getGroupeEtude() != null) {
            dto.setGroupeEtudeId(sessionEtude.getGroupeEtude().getId());
            dto.setGroupeEtudeNom(sessionEtude.getGroupeEtude().getNom());
        }

        return dto;
    }

    public SessionEtude toEntity(SessionEtudeDto dto, User user, Matiere matiere, GroupeEtude groupeEtude) {
        if (dto == null) {
            return null;
        }

        SessionEtude sessionEtude = new SessionEtude();
        sessionEtude.setId(dto.getId());
        sessionEtude.setTitre(dto.getTitre());
        sessionEtude.setDateDebut(dto.getDateDebut());
        sessionEtude.setDateFin(dto.getDateFin());
        sessionEtude.setDureeMax(dto.getDureeMax());
        sessionEtude.setStatut(dto.getStatut());
        sessionEtude.setPrivee(dto.getPrivee() == null ? true : dto.getPrivee());
        sessionEtude.setUser(user);
        sessionEtude.setMatiere(matiere);
        sessionEtude.setGroupeEtude(groupeEtude);

        return sessionEtude;
    }

    public void updateEntity(SessionEtude sessionEtude, SessionEtudeDto dto, User user, Matiere matiere, GroupeEtude groupeEtude) {
        if (sessionEtude == null || dto == null) {
            return;
        }

        sessionEtude.setTitre(dto.getTitre());
        sessionEtude.setDateDebut(dto.getDateDebut());
        sessionEtude.setDateFin(dto.getDateFin());
        sessionEtude.setDureeMax(dto.getDureeMax());
        sessionEtude.setStatut(dto.getStatut());
        if (dto.getPrivee() != null) {
            sessionEtude.setPrivee(dto.getPrivee());
        }
        sessionEtude.setUser(user == null ? sessionEtude.getUser() : user);
        sessionEtude.setMatiere(matiere == null ? sessionEtude.getMatiere() : matiere);
        sessionEtude.setGroupeEtude(groupeEtude == null ? sessionEtude.getGroupeEtude() : groupeEtude);
    }

    public List<SessionEtudeDto> toDtoList(List<SessionEtude> sessionsEtude) {
        if (sessionsEtude == null) {
            return List.of();
        }
        return sessionsEtude.stream()
                .map(this::toDto)
                .toList();
    }
}

