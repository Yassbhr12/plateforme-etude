package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupeEtudeRepo extends JpaRepository<GroupeEtude , Long> {
    Optional<GroupeEtude> findGroupeEtudeByNom(String nom);

    boolean existsGroupeEtudeByNom(String nom);

    List<GroupeEtude> findGroupeEtudeByAdmin(User admin);

}

