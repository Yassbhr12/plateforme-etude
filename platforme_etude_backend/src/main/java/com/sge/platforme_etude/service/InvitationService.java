package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.InvitationDto;
import com.sge.platforme_etude.entite.GroupeEtude;
import com.sge.platforme_etude.entite.Invitation;
import com.sge.platforme_etude.entite.Notification;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.StatutInvitation;
import com.sge.platforme_etude.helper.enums.TypeNotif;
import com.sge.platforme_etude.helper.exceptions.BadRequestException;
import com.sge.platforme_etude.helper.exceptions.ConflictException;
import com.sge.platforme_etude.helper.exceptions.ForbiddenException;
import com.sge.platforme_etude.helper.exceptions.NotFoundException;
import com.sge.platforme_etude.mapper.InvitationMapper;
import com.sge.platforme_etude.repository.GroupeEtudeRepo;
import com.sge.platforme_etude.repository.InvitationRepo;
import com.sge.platforme_etude.repository.NotificationRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvitationService {

    private final InvitationMapper mapper;
    private final InvitationRepo repo;
    private final GroupeEtudeRepo groupeEtudeRepo;
    private final UserRepo userRepo;
    private final NotificationRepo notificationRepo;

    public InvitationService(InvitationMapper mapper, InvitationRepo repo, GroupeEtudeRepo groupeEtudeRepo, UserRepo userRepo , NotificationRepo notificationRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.groupeEtudeRepo = groupeEtudeRepo;
        this.userRepo = userRepo;
        this.notificationRepo=notificationRepo;
    }

    @Transactional
    public InvitationDto createInvitation(InvitationDto dto) {
        if (dto.getSenderId() == null) {
            throw new BadRequestException("Sender id is required");
        }
        if (dto.getGroupeEtudeId() == null) {
            throw new BadRequestException("Groupe id is required");
        }
        GroupeEtude groupeEtude = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                .orElseThrow(() -> new NotFoundException("GroupeEtude Not Found"));
        User sender = userRepo.findById(dto.getSenderId())
                .orElseThrow(() -> new NotFoundException("Sender Not Found"));
        User receiver = resolveReceiver(dto);

        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Cannot invite yourself");
        }

        if (groupeEtude.getAdmin() == null || !groupeEtude.getAdmin().getId().equals(sender.getId())) {
            throw new ForbiddenException("Only group admin can invite members");
        }

        boolean alreadyMember = groupeEtude.getUsers() != null && groupeEtude.getUsers().stream()
                .anyMatch(u -> u.getId().equals(receiver.getId()));
        if (alreadyMember) {
            throw new ConflictException("Receiver is already a group member");
        }

        boolean pendingExists = repo.existsByGroupeEtudeIdAndReceiverIdAndStatut(
                groupeEtude.getId(),
                receiver.getId(),
                StatutInvitation.EN_ATTENTE
        );
        if (pendingExists) {
            throw new ConflictException("Pending invitation already exists");
        }

        Invitation invitation = mapper.toEntity(dto, groupeEtude, sender, receiver);
        invitation.setStatut(StatutInvitation.EN_ATTENTE);
        invitation.setDateEnvoi(LocalDateTime.now());
        Invitation saved = repo.save(invitation);

        Notification notification = new Notification();
        notification.setType(TypeNotif.INVITATION_GROUPE);
        notification.setMessage("Vous etes invites dans groupe "+groupeEtude.getNom()+" par l'Admin " + groupeEtude.getAdmin().getNom());
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setUser(receiver);
        notificationRepo.save(notification);

        return mapper.toDto(saved);
    }

    @Transactional
    public InvitationDto createInvitation(InvitationDto dto, Long currentUserId) {
        dto.setSenderId(currentUserId);
        return createInvitation(dto);
    }

    public InvitationDto findInvitationById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));
    }

    public List<InvitationDto> findAllInvitations() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<InvitationDto> findAllInvitationsBySenderId(Long senderId) {
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender Not Found"));

        return repo.findInvitationBySender(sender)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<InvitationDto> findAllInvitationsByReceiverId(Long receiverId) {
        User receiver = userRepo.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("Receiver Not Found"));

        return repo.findInvitationByReceiver(receiver)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public InvitationDto updateInvitationById(InvitationDto dto, Long id) {
        Invitation invitation = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));

        GroupeEtude groupeEtude = invitation.getGroupeEtude();
        if (dto.getGroupeEtudeId() != null) {
            groupeEtude = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                    .orElseThrow(() -> new NotFoundException("GroupeEtude Not Found"));
        }

        User sender = invitation.getSender();
        if (dto.getSenderId() != null) {
            sender = userRepo.findById(dto.getSenderId())
                    .orElseThrow(() -> new NotFoundException("Sender Not Found"));
        }

        User receiver = invitation.getReceiver();
        if (dto.getReceiverId() != null) {
            receiver = userRepo.findById(dto.getReceiverId())
                    .orElseThrow(() -> new NotFoundException("Receiver Not Found"));
        }

        mapper.updateEntity(invitation, dto, groupeEtude, sender, receiver);
        return mapper.toDto(repo.save(invitation));
    }

    @Transactional
    public void deleteInvitationById(Long id) {
        Invitation invitation = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));
        repo.delete(invitation);
    }

    @Transactional
    public InvitationDto accepterInvitation(Long invitationId, Long receiverId) {
        Invitation invitation = repo.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));

        if (invitation.getReceiver() == null || !invitation.getReceiver().getId().equals(receiverId)) {
            throw new ForbiddenException("Receiver mismatch");
        }

        if (invitation.getStatut() != StatutInvitation.EN_ATTENTE) {
            throw new ConflictException("Invitation is not pending");
        }

        GroupeEtude groupeEtude = invitation.getGroupeEtude();
        if (groupeEtude.getUsers() == null) {
            groupeEtude.setUsers(new ArrayList<>());
        }

        boolean alreadyMember = groupeEtude.getUsers().stream()
                .anyMatch(u -> u.getId().equals(receiverId));
        if (!alreadyMember) {
            groupeEtude.getUsers().add(invitation.getReceiver());
        }
        User receiver = invitation.getReceiver();
        if (receiver.getGroupeEtudes() == null) {
            receiver.setGroupeEtudes(new ArrayList<>());
        }
        boolean groupAlreadyLinked = receiver.getGroupeEtudes().stream()
                .anyMatch(g -> g.getId().equals(groupeEtude.getId()));
        if (!groupAlreadyLinked) {
            receiver.getGroupeEtudes().add(groupeEtude);
        }

        invitation.setStatut(StatutInvitation.ACCEPTEE);
        userRepo.save(receiver);
        groupeEtudeRepo.save(groupeEtude);
        return mapper.toDto(repo.save(invitation));
    }


    @Transactional
    public InvitationDto refuserInvitation(Long invitationId, Long receiverId) {
        Invitation invitation = repo.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));

        if (invitation.getReceiver() == null || !invitation.getReceiver().getId().equals(receiverId)) {
            throw new ForbiddenException("Receiver mismatch");
        }

        if (invitation.getStatut() != StatutInvitation.EN_ATTENTE) {
            throw new ConflictException("Invitation is not pending");
        }

        invitation.setStatut(StatutInvitation.REFUSEE);
        return mapper.toDto(repo.save(invitation));
    }


    @Transactional
    public InvitationDto annulerInvitation(Long invitationId , Long senderId){
        Invitation invitation = repo.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation Not Found"));

        if(invitation.getSender() == null || !invitation.getSender().getId().equals(senderId)){
            throw new ForbiddenException("Sender mismatch");
        }

        if(invitation.getStatut() != StatutInvitation.EN_ATTENTE){
            throw new ConflictException("Invitation is not pending");
        }

        invitation.setStatut(StatutInvitation.ANNULEE);

        return mapper.toDto(repo.save(invitation));
    }

    private User resolveReceiver(InvitationDto dto) {
        if (dto.getReceiverId() != null) {
            return userRepo.findById(dto.getReceiverId())
                    .orElseThrow(() -> new NotFoundException("Receiver Not Found"));
        }
        if (dto.getReceiverEmail() != null && !dto.getReceiverEmail().isBlank()) {
            return userRepo.findByEmailIgnoreCase(dto.getReceiverEmail().trim())
                    .orElseThrow(() -> new NotFoundException("Receiver Not Found"));
        }
        throw new BadRequestException("Receiver id or email is required");
    }

}
