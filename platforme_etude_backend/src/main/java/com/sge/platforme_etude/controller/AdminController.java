package com.sge.platforme_etude.controller;


import com.sge.platforme_etude.dto.DisponibiliteDto;
import com.sge.platforme_etude.dto.CommentaireDto;
import com.sge.platforme_etude.dto.GroupeEtudeDto;
import com.sge.platforme_etude.dto.InvitationDto;
import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.dto.MessageChatDto;
import com.sge.platforme_etude.dto.NotificationDto;
import com.sge.platforme_etude.dto.ObjectifHebdoDto;
import com.sge.platforme_etude.dto.SessionEtudeDto;
import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.service.CommentaireService;
import com.sge.platforme_etude.service.DisponibiliteService;
import com.sge.platforme_etude.service.GroupeEtudeService;
import com.sge.platforme_etude.service.InvitationService;
import com.sge.platforme_etude.service.MatiereService;
import com.sge.platforme_etude.service.MessageChatService;
import com.sge.platforme_etude.service.NotificationService;
import com.sge.platforme_etude.service.ObjectifHebdoService;
import com.sge.platforme_etude.service.SessionEtudeService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import com.sge.platforme_etude.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final MatiereService matiereService;
    private final DisponibiliteService disponibiliteService;
    private final ObjectifHebdoService objectifHebdoService;
    private final GroupeEtudeService groupeEtudeService;
    private final InvitationService invitationService;
    private final MessageChatService messageChatService;
    private final NotificationService notificationService;
    private final CommentaireService commentaireService;
    private final SessionEtudeService sessionEtudeService;

    //Users
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        List<UserDto> userDtos = userService.findAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(userDtos);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        UserDto dto = userService.findUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body("L'utilisateur avec l'id " + id + " a ete supprime avec succes");
    }

    //Matieres
    @GetMapping("/matieres")
    public ResponseEntity<List<MatiereDto>> getAllMatieres(){
        List<MatiereDto> matiereDtos = matiereService.findAllMatieres();
        return ResponseEntity.status(HttpStatus.OK).body(matiereDtos);
    }

    @GetMapping("/matieres/{id}")
    public ResponseEntity<MatiereDto> getMatiereById(@PathVariable Long id){
        MatiereDto dto = matiereService.findMatiereById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @DeleteMapping("/matieres/{id}")
    public ResponseEntity<String> deleteMatiereById(@PathVariable Long id){
        matiereService.deleteMatiereById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("La matiere aved l'id "+ id + " a ete supprime avec succes");
    }

    // Disponibilites

    @GetMapping("/disponibilites")
    public ResponseEntity<List<DisponibiliteDto>> getAllDisponibilites(){
        return ResponseEntity.status(HttpStatus.OK).body(disponibiliteService.findAllDispo());
    }

    @GetMapping("/disponibilites/{id}")
    public ResponseEntity<DisponibiliteDto> getDisponibiliteById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(disponibiliteService.findDispoById(id));
    }

    @DeleteMapping("/disponibilites/{id}")
    public ResponseEntity<String> deleteDisponibilitesById(@PathVariable Long id){
        disponibiliteService.deleteDispoById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Disponibilite is deleted successfully");
    }

    // Objectifs

    @GetMapping("/objectifs")
    public ResponseEntity<List<ObjectifHebdoDto>> getObjectifsHebdo(){
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.findAllObjectifsHebdo());
    }

    @GetMapping("/objectifs/{id}")
    public ResponseEntity<ObjectifHebdoDto> getObjectifsHebdoById(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.findObjectifHebdoById(id));
    }

    @GetMapping("/matieres/{matiereId}/objectifs")
    public ResponseEntity<List<ObjectifHebdoDto>> getObjectifsHebdoByMatiereId(@PathVariable Long matiereId) {
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.findAllObjectifsHebdoByMatiereId(matiereId));
    }

    @DeleteMapping("/objectifs/{id}")
    public ResponseEntity<String> deleteObjectifHebdoById(@PathVariable Long id){
        objectifHebdoService.deleteObjectifHebdoById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Objectif is deleted successfully");
    }

    // Groupes
    @GetMapping("/groupes")
    public ResponseEntity<List<GroupeEtudeDto>> getAllGroupes() {
        return ResponseEntity.status(HttpStatus.OK).body(groupeEtudeService.findAllGroupesEtude());
    }

    @GetMapping("/groupes/{id}")
    public ResponseEntity<GroupeEtudeDto> getGroupeById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(groupeEtudeService.findGroupeEtudeById(id));
    }

    @DeleteMapping("/groupes/{id}")
    public ResponseEntity<String> deleteGroupeById(@PathVariable Long id) {
        groupeEtudeService.deleteGroupeEtudeById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Groupe is deleted successfully");
    }

    // Invitations
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationDto>> getAllInvitations() {
        return ResponseEntity.status(HttpStatus.OK).body(invitationService.findAllInvitations());
    }

    @GetMapping("/invitations/{id}")
    public ResponseEntity<InvitationDto> getInvitationById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(invitationService.findInvitationById(id));
    }

    @DeleteMapping("/invitations/{id}")
    public ResponseEntity<String> deleteInvitationById(@PathVariable Long id) {
        invitationService.deleteInvitationById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Invitation is deleted successfully");
    }

    // Messages
    @GetMapping("/messages")
    public ResponseEntity<List<MessageChatDto>> getAllMessages() {
        return ResponseEntity.status(HttpStatus.OK).body(messageChatService.findAllMessagesChat());
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageChatDto> getMessageById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(messageChatService.findMessageChatById(id));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<String> deleteMessageById(@PathVariable Long id) {
        messageChatService.deleteMessageChatById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Message is deleted successfully");
    }

    // Notifications
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.findAllNotif());
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.findNotificationById(id));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<String> deleteNotificationById(@PathVariable Long id) {
        notificationService.deleteNotificationById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Notification is deleted successfully");
    }

    // Commentaires
    @GetMapping("/commentaires")
    public ResponseEntity<List<CommentaireDto>> getAllCommentaires() {
        return ResponseEntity.status(HttpStatus.OK).body(commentaireService.findAllCommentaires());
    }

    @GetMapping("/commentaires/{id}")
    public ResponseEntity<CommentaireDto> getCommentaireById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(commentaireService.findCommentaireById(id));
    }

    @DeleteMapping("/commentaires/{id}")
    public ResponseEntity<String> deleteCommentaireById(@PathVariable Long id) {
        commentaireService.deleteCommentaireById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Commentaire is deleted successfully");
    }

    // Sessions
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionEtudeDto>> getAllSessions() {
        return ResponseEntity.status(HttpStatus.OK).body(sessionEtudeService.findAllSessionsEtude());
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<SessionEtudeDto> getSessionById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(sessionEtudeService.findSessionEtudeById(id));
    }

    @GetMapping("/matieres/{matiereId}/sessions")
    public ResponseEntity<List<SessionEtudeDto>> getSessionsByMatiereId(@PathVariable Long matiereId) {
        return ResponseEntity.status(HttpStatus.OK).body(sessionEtudeService.findAllSessionsEtudeByMatiereId(matiereId));
    }

    @GetMapping("/groupes/{groupeEtudeId}/sessions")
    public ResponseEntity<List<SessionEtudeDto>> getSessionsByGroupeId(@PathVariable Long groupeEtudeId) {
        return ResponseEntity.status(HttpStatus.OK).body(sessionEtudeService.findAllSessionsEtudeByGroupeEtudeId(groupeEtudeId));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<String> deleteSessionById(@PathVariable Long id) {
        sessionEtudeService.deleteSessionEtudeById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Session is deleted successfully");
    }

}
