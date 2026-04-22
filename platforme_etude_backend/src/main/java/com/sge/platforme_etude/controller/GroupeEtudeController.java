package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.GroupeEtudeDto;
import com.sge.platforme_etude.service.GroupeEtudeService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/groupes")
public class GroupeEtudeController {

    private final GroupeEtudeService service;
    private final CurrentUserService currentUserService;

    public GroupeEtudeController(GroupeEtudeService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/admin")
    public ResponseEntity<List<GroupeEtudeDto>> getMyAdminGroupes() {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllGroupesEtudeByAdminId(currentUserService.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<GroupeEtudeDto> createGroupeEtude(@Valid @RequestBody GroupeEtudeDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGroupeEtudeForCurrentUser(dto, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupeEtudeDto> updateGroupeEtudeById(@PathVariable Long id , @Valid @RequestBody GroupeEtudeDto dto){
        return ResponseEntity.status(HttpStatus.OK).body(service.updateGroupeEtudeByIdForCurrentUser(dto, id, currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGroupeEtudeById(@PathVariable Long id){
        service.deleteGroupeEtudeByIdForCurrentUser(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Groupe is deleted successfully");
    }


}
