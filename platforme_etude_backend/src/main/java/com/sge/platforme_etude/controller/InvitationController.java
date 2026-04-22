package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.InvitationDto;
import com.sge.platforme_etude.service.InvitationService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/invitations")
public class InvitationController {

    private final InvitationService service;
    private final CurrentUserService currentUserService;

    public InvitationController(InvitationService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<List<InvitationDto>> getMyReceivedInvitations(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllInvitationsByReceiverId(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<InvitationDto>> getMySentInvitations() {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllInvitationsBySenderId(currentUserService.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<InvitationDto> createInvitation(@Valid @RequestBody InvitationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInvitation(dto, currentUserService.getCurrentUserId()));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<InvitationDto> accepterInvitation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.accepterInvitation(id, currentUserService.getCurrentUserId()));
    }

    @PatchMapping("/{id}/refuse")
    public ResponseEntity<InvitationDto> refuserInvitation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.refuserInvitation(id, currentUserService.getCurrentUserId()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvitationDto> annulerInvitation(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.annulerInvitation(id, currentUserService.getCurrentUserId()));
    }
}
