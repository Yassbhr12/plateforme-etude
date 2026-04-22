package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibiliteRepo extends JpaRepository<Disponibilite , Long> {
    List<Disponibilite> findDisponibiliteByUserId(Long userId);

}
