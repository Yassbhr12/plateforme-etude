package com.sge.platforme_etude.mapper;

import com.sge.platforme_etude.dto.CommentaireDto;
import com.sge.platforme_etude.entite.Commentaire;
import com.sge.platforme_etude.entite.SessionEtude;
import com.sge.platforme_etude.entite.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentaireMapper {

    public CommentaireDto toDto(Commentaire commentaire) {
        if (commentaire == null) {
            return null;
        }

        CommentaireDto dto = new CommentaireDto();
        dto.setId(commentaire.getId());
        dto.setContenu(commentaire.getContenu());

        if (commentaire.getUser() != null) {
            dto.setUserId(commentaire.getUser().getId());
            dto.setUserNom(commentaire.getUser().getNom());
            dto.setUserEmail(commentaire.getUser().getEmail());
        }

        if (commentaire.getSessionEtude() != null) {
            dto.setSessionEtudeId(commentaire.getSessionEtude().getId());
            dto.setSessionEtudeTitre(commentaire.getSessionEtude().getTitre());
        }

        return dto;
    }

    public Commentaire toEntity(CommentaireDto dto, User user, SessionEtude sessionEtude) {
        if (dto == null) {
            return null;
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setId(dto.getId());
        commentaire.setContenu(dto.getContenu());
        commentaire.setUser(user);
        commentaire.setSessionEtude(sessionEtude);

        return commentaire;
    }

    public void updateEntity(Commentaire commentaire, CommentaireDto dto, User user, SessionEtude sessionEtude) {
        if (commentaire == null || dto == null) {
            return;
        }

        commentaire.setContenu(dto.getContenu());
        commentaire.setUser(user == null ? commentaire.getUser() : user);
        commentaire.setSessionEtude(sessionEtude == null ? commentaire.getSessionEtude() : sessionEtude);
    }

    public List<CommentaireDto> toDtoList(List<Commentaire> commentaires) {
        if (commentaires == null) {
            return List.of();
        }
        return commentaires.stream()
                .map(this::toDto)
                .toList();
    }
}

