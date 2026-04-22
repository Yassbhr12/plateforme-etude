package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentaireRepo extends JpaRepository<Commentaire , Long> {
    List<Commentaire> findCommentaireByUserId(Long userId);

    List<Commentaire> findCommentaireBySessionEtudeId(Long sessionEtudeId);
}

