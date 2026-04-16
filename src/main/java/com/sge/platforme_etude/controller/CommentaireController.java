package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.CommentaireDto;
import com.sge.platforme_etude.service.CommentaireService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentaireController {

    private final CommentaireService service;
    private final CurrentUserService currentUserService;

    public CommentaireController(CommentaireService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }


    @GetMapping("/me/commentaires")
    public ResponseEntity<List<CommentaireDto>> getMyCommentaires() {
        return ResponseEntity.status(HttpStatus.OK).body(service.findMyCommentaires(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/sessions/{sessionEtudeId}/commentaires")
    public ResponseEntity<List<CommentaireDto>> getCommentairesBySessionEtudeId(@PathVariable Long sessionEtudeId) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllCommentairesBySessionEtudeId(sessionEtudeId));
    }

    @PostMapping("/commentaires")
    public ResponseEntity<CommentaireDto> createCommentaire(@Valid @RequestBody CommentaireDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCommentaire(dto, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/commentaires/{id}")
    public ResponseEntity<CommentaireDto> updateCommentaireById(@PathVariable Long id, @Valid @RequestBody CommentaireDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(service.updateCommentaireById(dto, id, currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/commentaires/{id}")
    public ResponseEntity<String> deleteCommentaireById(@PathVariable Long id) {
        service.deleteCommentaireById(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Commentaire is deleted successfully");
    }
}

