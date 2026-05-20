package com.sge.platforme_etude;

import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.*;
import org.springframework.stereotype.Component;

import java.time.*;

/**
 * Classe utilitaire pour créer des objets de test réutilisables
 * dans l'ensemble des tests d'intégration.
 */
@Component
public class TestDataFactory {

    // ─── User ────────────────────────────────────────────────────
    public static User createUser(String nom, String prenom, String email) {
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMotDePasse("$2a$10$hashedpassword1234567890123456"); // BCrypt hash factice
        user.setRole(Role.ROLE_USER);
        user.setActif(true);
        return user;
    }

    public static User createAdmin(String nom, String prenom, String email) {
        User user = createUser(nom, prenom, email);
        user.setRole(Role.ROLE_ADMIN);
        return user;
    }

    // ─── Matiere ─────────────────────────────────────────────────
    public static Matiere createMatiere(String nom, Integer priorite, User user) {
        Matiere matiere = new Matiere();
        matiere.setNom(nom);
        matiere.setPriorite(priorite);
        matiere.setUser(user);
        return matiere;
    }

    // ─── GroupeEtude ─────────────────────────────────────────────
    public static GroupeEtude createGroupeEtude(String nom, String description, User admin) {
        GroupeEtude groupe = new GroupeEtude();
        groupe.setNom(nom);
        groupe.setDescription(description);
        groupe.setAdmin(admin);
        return groupe;
    }

    // ─── SessionEtude ────────────────────────────────────────────
    public static SessionEtude createSessionEtude(String titre, User user, Matiere matiere,
                                                   StatutSession statut) {
        SessionEtude session = new SessionEtude();
        session.setTitre(titre);
        session.setDateDebut(LocalDateTime.now().plusHours(1));
        session.setDateFin(LocalDateTime.now().plusHours(3));
        session.setDureeMax(120);
        session.setStatut(statut);
        session.setPrivee(true);
        session.setUser(user);
        session.setMatiere(matiere);
        return session;
    }

    // ─── Commentaire ─────────────────────────────────────────────
    public static Commentaire createCommentaire(String contenu, User user, SessionEtude session) {
        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(contenu);
        commentaire.setUser(user);
        commentaire.setSessionEtude(session);
        return commentaire;
    }

    // ─── Disponibilite ──────────────────────────────────────────
    public static Disponibilite createDisponibilite(Integer jourSemaine,
                                                     LocalTime heureDebut,
                                                     LocalTime heureFin,
                                                     User user) {
        Disponibilite dispo = new Disponibilite();
        dispo.setJourSemaine(jourSemaine);
        dispo.setHeureDebut(heureDebut);
        dispo.setHeureFin(heureFin);
        dispo.setUser(user);
        return dispo;
    }

    // ─── ObjectifHebdo ──────────────────────────────────────────
    public static ObjectifHebdo createObjectifHebdo(LocalDate semaine, Integer heuresCibles,
                                                     User user, Matiere matiere) {
        ObjectifHebdo objectif = new ObjectifHebdo();
        objectif.setSemaine(semaine);
        objectif.setHeuresCibles(heuresCibles);
        objectif.setUser(user);
        objectif.setMatiere(matiere);
        return objectif;
    }

    // ─── Notification ───────────────────────────────────────────
    public static Notification createNotification(TypeNotif type, String message, User user) {
        Notification notif = new Notification();
        notif.setType(type);
        notif.setMessage(message);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setLue(false);
        notif.setUser(user);
        return notif;
    }

    // ─── Invitation ─────────────────────────────────────────────
    public static Invitation createInvitation(StatutInvitation statut,
                                               GroupeEtude groupe,
                                               User sender,
                                               User receiver) {
        Invitation invitation = new Invitation();
        invitation.setStatut(statut);
        invitation.setDateEnvoi(LocalDateTime.now());
        invitation.setGroupeEtude(groupe);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        return invitation;
    }

    // ─── MessageChat ────────────────────────────────────────────
    public static MessageChat createMessageChat(String contenu, User user, GroupeEtude groupe) {
        MessageChat msg = new MessageChat();
        msg.setContenu(contenu);
        msg.setDateEnvoi(LocalDateTime.now());
        msg.setUser(user);
        msg.setGroupeEtude(groupe);
        return msg;
    }

    // ─── RefreshToken ───────────────────────────────────────────
    public static RefreshToken createRefreshToken(String token, User user) {
        return RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
    }
}
