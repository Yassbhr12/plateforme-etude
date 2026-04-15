package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.entite.Invitation;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.StatutInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepo extends JpaRepository<Invitation , Long> {
    List<Invitation> findInvitationBySender(User sender);

    List<Invitation> findInvitationByReceiver(User receiver);

    List<Invitation> findInvitationByStatut(StatutInvitation statut);

    List<Invitation> findInvitationByGroupeEtudeId(Long groupeEtudeId);

    boolean existsByGroupeEtudeIdAndReceiverIdAndStatut(Long id, Long candidateId, StatutInvitation statutInvitation);
}

