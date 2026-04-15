package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.ObjectifHebdoDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.ObjectifHebdo;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObjectifHebdoMapper {

    public ObjectifHebdoDto toDto(ObjectifHebdo objectifHebdo) {
        if (objectifHebdo == null) {
            return null;
        }

        ObjectifHebdoDto dto = new ObjectifHebdoDto();
        dto.setId(objectifHebdo.getId());
        dto.setSemaine(objectifHebdo.getSemaine());
        dto.setHeuresCibles(objectifHebdo.getHeuresCibles());

        if (objectifHebdo.getUser() != null) {
            dto.setUserId(objectifHebdo.getUser().getId());
            dto.setUserNom(objectifHebdo.getUser().getNom());
            dto.setUserEmail(objectifHebdo.getUser().getEmail());
        }

        if (objectifHebdo.getMatiere() != null) {
            dto.setMatiereId(objectifHebdo.getMatiere().getId());
            dto.setMatiereNom(objectifHebdo.getMatiere().getNom());
        }

        return dto;
    }

    public ObjectifHebdo toEntity(ObjectifHebdoDto dto, User user, Matiere matiere) {
        if (dto == null) {
            return null;
        }

        ObjectifHebdo objectifHebdo = new ObjectifHebdo();
        objectifHebdo.setId(dto.getId());
        objectifHebdo.setSemaine(dto.getSemaine());
        objectifHebdo.setHeuresCibles(dto.getHeuresCibles());
        objectifHebdo.setUser(user);
        objectifHebdo.setMatiere(matiere);

        return objectifHebdo;
    }

    public void updateEntity(ObjectifHebdo objectifHebdo, ObjectifHebdoDto dto, User user, Matiere matiere) {
        if (objectifHebdo == null || dto == null) {
            return;
        }

        objectifHebdo.setSemaine(dto.getSemaine());
        objectifHebdo.setHeuresCibles(dto.getHeuresCibles());
        objectifHebdo.setUser(user == null ? objectifHebdo.getUser() : user);
        objectifHebdo.setMatiere(matiere == null ? objectifHebdo.getMatiere() : matiere);
    }

    public List<ObjectifHebdoDto> toDtoList(List<ObjectifHebdo> objectifsHebdo) {
        if (objectifsHebdo == null) {
            return List.of();
        }
        return objectifsHebdo.stream()
                .map(this::toDto)
                .toList();
    }
}

