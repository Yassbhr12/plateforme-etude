package com.sge.platforme_etude.repository;

import com.sge.platforme_etude.AbstractIntegrationTest;
import com.sge.platforme_etude.TestDataFactory;
import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.StatutInvitation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class InvitationRepoIntegrationTest extends AbstractIntegrationTest {

    @Autowired private InvitationRepo invitationRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private GroupeEtudeRepo groupeEtudeRepo;

    private User sender;
    private User receiver;
    private GroupeEtude groupe;

    @BeforeEach
    void setUp() {
        invitationRepo.deleteAll();
        groupeEtudeRepo.deleteAll();
        userRepo.deleteAll();
        sender = userRepo.save(TestDataFactory.createUser("Sender", "Test", "sender@test.com"));
        receiver = userRepo.save(TestDataFactory.createUser("Receiver", "Test", "receiver@test.com"));
        groupe = groupeEtudeRepo.save(TestDataFactory.createGroupeEtude("Groupe1", "desc", sender));
    }

    @Test
    @DisplayName("Sauvegarder et retrouver une Invitation")
    void shouldSaveAndFind() {
        Invitation inv = invitationRepo.save(
                TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        assertThat(invitationRepo.findById(inv.getId())).isPresent();
    }

    @Test
    @DisplayName("Trouver par sender")
    void shouldFindBySender() {
        invitationRepo.save(TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        List<Invitation> result = invitationRepo.findInvitationBySender(sender);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par receiver")
    void shouldFindByReceiver() {
        invitationRepo.save(TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        List<Invitation> result = invitationRepo.findInvitationByReceiver(receiver);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Trouver par statut")
    void shouldFindByStatut() {
        invitationRepo.save(TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        assertThat(invitationRepo.findInvitationByStatut(StatutInvitation.EN_ATTENTE)).hasSize(1);
        assertThat(invitationRepo.findInvitationByStatut(StatutInvitation.ACCEPTEE)).isEmpty();
    }

    @Test
    @DisplayName("Trouver par groupeEtudeId")
    void shouldFindByGroupeEtudeId() {
        invitationRepo.save(TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        assertThat(invitationRepo.findInvitationByGroupeEtudeId(groupe.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Vérifier l'existence d'une invitation en attente")
    void shouldCheckExistsByGroupeAndReceiverAndStatut() {
        invitationRepo.save(TestDataFactory.createInvitation(StatutInvitation.EN_ATTENTE, groupe, sender, receiver));
        assertThat(invitationRepo.existsByGroupeEtudeIdAndReceiverIdAndStatut(
                groupe.getId(), receiver.getId(), StatutInvitation.EN_ATTENTE)).isTrue();
        assertThat(invitationRepo.existsByGroupeEtudeIdAndReceiverIdAndStatut(
                groupe.getId(), receiver.getId(), StatutInvitation.ACCEPTEE)).isFalse();
    }
}
