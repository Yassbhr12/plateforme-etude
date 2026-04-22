package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatiereRepo extends JpaRepository<Matiere , Long> {
    Optional<Matiere> findMatiereByNom(String nom);

    List<Matiere> findMatiereByUserId(Long userId);
}
