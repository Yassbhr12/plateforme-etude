package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.GroupeEtudeDto;
import com.sge.platforme_etude.dto.UserDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.Invitation;
import com.sge.platforme_etude.entite.Notification;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.StatutInvitation;
import com.sge.platforme_etude.helper.enums.TypeNotif;
import com.sge.platforme_etude.mapper.GroupeEtudeMapper;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.InvitationRepo;
import com.sge.platforme_etude.repository.NotificationRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.*;

@Service
public class GroupeEtudeService {

    private final GroupeEtudeMapper mapper;
    private final GroupeEtudeRepo repo;
    private final UserRepo userRepo;
    private final NotificationRepo notificationRepo;
    private final InvitationRepo invitationRepo;

    public GroupeEtudeService(GroupeEtudeMapper mapper, GroupeEtudeRepo repo, UserRepo userRepo, NotificationRepo notificationRepo , InvitationRepo invitationRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
        this.notificationRepo = notificationRepo;
        this.invitationRepo=invitationRepo;

    }

    @Transactional
    public GroupeEtudeDto createGroupeEtude(GroupeEtudeDto dto) {
        if (dto.getAdminId() == null) {
            throw new RuntimeException("Admin is required");
        }

        User admin = userRepo.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin Not Found"));

        List<User> users = dto.getUsers() == null ? new ArrayList<>() : dto.getUsers().stream()
                .map(u -> userRepo.findById(u.getId()).orElseThrow(() -> new RuntimeException("User Not Found")))
                .distinct()
                .toList();

//        if (users.stream().noneMatch(u -> u.getId().equals(admin.getId()))) {
//            List<User> mutableUsers = new ArrayList<>(users);
//            mutableUsers.add(admin);
//            users = mutableUsers;
//        }

        List<User> membres = new ArrayList<>();
        membres.add(admin);

        GroupeEtude groupeEtude = mapper.toEntity(dto, admin, membres);

        GroupeEtude saved = repo.save(groupeEtude);

        if(!users.isEmpty()){
            List<Notification> notificationList = new ArrayList<>();
            List<Invitation> invitations = new ArrayList<>();

            for (User user: users){
                if (!user.getId().equals(admin.getId())){
                    Notification notification = new Notification();
                    notification.setType(TypeNotif.INVITATION_GROUPE);
                    notification.setMessage("Vous etes invites dans groupe "+dto.getNom()+" par l'Admin " + admin.getNom());
                    notification.setDateEnvoi(LocalDateTime.now());
                    notification.setUser(user);
                    notificationList.add(notification);

                    Invitation invitation = new Invitation();
                    invitation.setStatut(StatutInvitation.EN_ATTENTE);
                    invitation.setDateEnvoi(LocalDateTime.now());
                    invitation.setGroupeEtude(saved);
                    invitation.setSender(admin);
                    invitation.setReceiver(user);
                    invitations.add(invitation);

                }
            }

            invitationRepo.saveAll(invitations);
            notificationRepo.saveAll(notificationList);


        }

        return mapper.toDto(saved);
    }

    @Transactional
    public GroupeEtudeDto createGroupeEtudeForCurrentUser(GroupeEtudeDto dto, Long currentUserId) {
        dto.setAdminId(currentUserId);
        return createGroupeEtude(dto);
    }

    public GroupeEtudeDto findGroupeEtudeById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
    }

    public List<GroupeEtudeDto> findAllGroupesEtude() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<GroupeEtudeDto> findAllGroupesEtudeByAdminId(Long adminId){
        User admin = userRepo.findById(adminId)
                .orElseThrow(()->new RuntimeException("Admin Not Found"));

        List<GroupeEtude> groupeEtudes = repo.findGroupeEtudeByAdmin(admin);

        return repo.findGroupeEtudeByAdmin(admin)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public GroupeEtudeDto updateGroupeEtudeById(GroupeEtudeDto dto, Long id) {
        GroupeEtude groupeEtude = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));

        User admin = groupeEtude.getAdmin();
//        if (dto.getAdminId() != null && !dto.getAdminId().equals(admin.getId())) {
//            admin = userRepo.findById(dto.getAdminId())
//                    .orElseThrow(() -> new RuntimeException("Admin Not Found"));
//        }

        if (dto.getNom() != null && !dto.getNom().isBlank()) {
            groupeEtude.setNom(dto.getNom());
        }

        if (dto.getDescription() != null) {
            groupeEtude.setDescription(dto.getDescription());
        }
        groupeEtude.setAdmin(admin);


        if (dto.getUsers() != null && !dto.getUsers().isEmpty()) {


            List<User> membresActuels = groupeEtude.getUsers() == null ? List.of() : groupeEtude.getUsers();
            Set<Long> memberIds = membresActuels.stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());

            Set<Long> uniqueCandidateIds = new HashSet<>();
            for (UserDto userDto : dto.getUsers()) {
                if (userDto != null && userDto.getId() != null) {
                    uniqueCandidateIds.add(userDto.getId());
                }
            }

            List<Invitation> invitationsToSave = new java.util.ArrayList<>();
            List<Notification> notificationsToSave = new java.util.ArrayList<>();

            for (Long candidateId : uniqueCandidateIds) {

                // Ne pas inviter l'admin
                if (candidateId.equals(admin.getId())) {
                    continue;
                }
                // Ne pas inviter un membre deja dans le groupe
                if (memberIds.contains(candidateId)) {
                    continue;
                }
                // Ne pas dupliquer une invitation EN_ATTENTE
                boolean pendingExists = invitationRepo.existsByGroupeEtudeIdAndReceiverIdAndStatut(
                        groupeEtude.getId(),
                        candidateId,
                        StatutInvitation.EN_ATTENTE
                );
                if (pendingExists) {
                    continue;
                }
                User receiver = userRepo.findById(candidateId)
                        .orElseThrow(() -> new RuntimeException("User Not Found: "));

                Invitation invitation = new Invitation();
                invitation.setStatut(StatutInvitation.EN_ATTENTE);
                invitation.setDateEnvoi(LocalDateTime.now());
                invitation.setGroupeEtude(groupeEtude);
                invitation.setSender(admin);
                invitation.setReceiver(receiver);
                invitationsToSave.add(invitation);

                Notification notification = new Notification();
                notification.setType(TypeNotif.INVITATION_GROUPE);
                notification.setMessage("Vous êtes invité dans le groupe " + groupeEtude.getNom()
                        + " par l'Admin " + admin.getNom());
                notification.setDateEnvoi(LocalDateTime.now());
                notification.setUser(receiver);
                notificationsToSave.add(notification);
            }

            if (!invitationsToSave.isEmpty()) {
                invitationRepo.saveAll(invitationsToSave);
            }
            if (!notificationsToSave.isEmpty()) {
                notificationRepo.saveAll(notificationsToSave);
            }
        }


        GroupeEtude updated = repo.save(groupeEtude);
        return mapper.toDto(updated);
    }

    @Transactional
    public GroupeEtudeDto updateGroupeEtudeByIdForCurrentUser(GroupeEtudeDto dto, Long id, Long currentUserId) {
        GroupeEtude groupeEtude = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
        if (groupeEtude.getAdmin() == null || !groupeEtude.getAdmin().getId().equals(currentUserId)) {
            throw new RuntimeException("Only group admin can update this group");
        }
        return updateGroupeEtudeById(dto, id);
    }

    @Transactional
    public void deleteGroupeEtudeById(Long id) {
        GroupeEtude groupeEtude = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
        repo.delete(groupeEtude);
    }

    @Transactional
    public void deleteGroupeEtudeByIdForCurrentUser(Long id, Long currentUserId) {
        GroupeEtude groupeEtude = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
        if (groupeEtude.getAdmin() == null || !groupeEtude.getAdmin().getId().equals(currentUserId)) {
            throw new RuntimeException("Only group admin can delete this group");
        }
        repo.delete(groupeEtude);
    }
}
