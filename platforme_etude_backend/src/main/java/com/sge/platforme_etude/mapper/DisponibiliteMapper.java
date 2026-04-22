package com.sge.platforme_etude.mapper;


import com.sge.platforme_etude.dto.DisponibiliteDto;
import com.sge.platforme_etude.entite.Disponibilite;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DisponibiliteMapper {

    public DisponibiliteDto toDto(Disponibilite disponibilite) {
        if (disponibilite == null) {
            return null;
        }

        DisponibiliteDto dto = new DisponibiliteDto();
        dto.setId(disponibilite.getId());
        dto.setJourSemaine(disponibilite.getJourSemaine());
        dto.setHeureDebut(disponibilite.getHeureDebut());
        dto.setHeureFin(disponibilite.getHeureFin());

        if (disponibilite.getUser() != null) {
            dto.setUserId(disponibilite.getUser().getId());
            dto.setUserNom(disponibilite.getUser().getNom());
            dto.setUserEmail(disponibilite.getUser().getEmail());
        }

        return dto;
    }

    public Disponibilite toEntity(DisponibiliteDto dto, User user) {
        if (dto == null) {
            return null;
        }

        Disponibilite disponibilite = new Disponibilite();
        disponibilite.setId(dto.getId());
        disponibilite.setJourSemaine(dto.getJourSemaine());
        disponibilite.setHeureDebut(dto.getHeureDebut());
        disponibilite.setHeureFin(dto.getHeureFin());
        disponibilite.setUser(user);

        return disponibilite;
    }

    public void updateEntity(Disponibilite disponibilite, DisponibiliteDto dto, User user) {
        if (disponibilite == null || dto == null) {
            return;
        }

        disponibilite.setJourSemaine(dto.getJourSemaine());
        disponibilite.setHeureDebut(dto.getHeureDebut());
        disponibilite.setHeureFin(dto.getHeureFin());
        disponibilite.setUser(user == null ? disponibilite.getUser() : user);
    }

    public List<DisponibiliteDto> toDtoList(List<Disponibilite> disponibilites) {
        if (disponibilites == null) {
            return List.of();
        }
        return disponibilites.stream()
                .map(this::toDto)
                .toList();
    }
}
