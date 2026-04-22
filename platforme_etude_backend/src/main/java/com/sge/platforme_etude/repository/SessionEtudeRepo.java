package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.SessionEtude;
import com.sge.platforme_etude.helper.enums.StatutSession;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionEtudeRepo extends JpaRepository<SessionEtude , Long> {
    List<SessionEtude> findSessionEtudeByUserId(Long userId);

    List<SessionEtude> findSessionEtudeByMatiereId(Long matiereId);

    List<SessionEtude> findSessionEtudeByGroupeEtudeId(Long groupeEtudeId);

    List<SessionEtude> findSessionEtudeByStatut(StatutSession statut);

//Ca te permet de recuperer toutes les sessions de l’utilisateur dans une semaine
    List<SessionEtude> findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(Long userId, LocalDateTime start, LocalDateTime end);

    void deleteByUserIdAndStatutAndDateDebutGreaterThanEqualAndDateDebutLessThan(Long userId, StatutSession statut, LocalDateTime start, LocalDateTime end);


}

