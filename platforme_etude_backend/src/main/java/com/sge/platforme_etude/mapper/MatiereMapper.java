package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatiereMapper {

    public MatiereDto toDto(Matiere matiere) {
        if (matiere == null) {
            return null;
        }

        MatiereDto matiereDto = new MatiereDto();

        matiereDto.setId(matiere.getId());
        matiereDto.setNom(matiere.getNom());
        matiereDto.setPriorite(matiere.getPriorite());

        if (matiere.getUser() != null) {
            matiereDto.setUserId(matiere.getUser().getId());
            matiereDto.setUserNom(matiere.getUser().getNom());
            matiereDto.setUserEmail(matiere.getUser().getEmail());
        }


        return matiereDto;
    }

    public Matiere toEntity(MatiereDto matiereDto, User user) {
        if (matiereDto == null) {
            return null;
        }

        Matiere matiere = new Matiere();
        matiere.setId(matiereDto.getId());
        matiere.setNom(matiereDto.getNom());
        matiere.setPriorite(matiereDto.getPriorite());
        matiere.setUser(user);

        return matiere;
    }

    public List<MatiereDto> toDtoList(List<Matiere> matieres) {
        if (matieres == null) {
            return List.of();
        }
        return matieres.stream()
                .map(this::toDto)
                .toList();
    }

    public void updateEntity(Matiere matiere, MatiereDto matiereDto, User user) {
        if (matiere == null || matiereDto == null) {
            return;
        }

        matiere.setNom(matiereDto.getNom());
        matiere.setPriorite(matiereDto.getPriorite());
        matiere.setUser(user == null ? matiere.getUser() : user);
    }
}
