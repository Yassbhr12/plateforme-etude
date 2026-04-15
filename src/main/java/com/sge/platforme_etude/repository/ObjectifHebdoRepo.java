package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.ObjectifHebdo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObjectifHebdoRepo extends JpaRepository<ObjectifHebdo , Long> {
    List<ObjectifHebdo> findObjectifHebdoByUserId(Long userId);

    List<ObjectifHebdo> findObjectifHebdoByMatiereId(Long matiereId);

    Optional<ObjectifHebdo> findByUserIdAndMatiereIdAndSemaine(Long userId, Long matiereId, LocalDate semaine);

    boolean existsByUserIdAndMatiereIdAndSemaine(Long userId, Long matiereId, LocalDate semaine);

    List<ObjectifHebdo> findObjectifHebdoBySemaine(LocalDate semaine);

    List<ObjectifHebdo> findByUserIdAndSemaine(Long userId, LocalDate semaine);
    List<ObjectifHebdo> findObjectifHebdoByUserIdAndMatiereId(Long user_id, Long matiere_id);
}

