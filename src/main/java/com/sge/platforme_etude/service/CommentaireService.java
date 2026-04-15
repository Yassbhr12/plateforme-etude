package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.CommentaireDto;
import com.sge.platforme_etude.entite.Commentaire;
import com.sge.platforme_etude.entite.SessionEtude;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.mapper.CommentaireMapper;
import com.sge.platforme_etude.repository.CommentaireRepo;
import com.sge.platforme_etude.repository.SessionEtudeRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentaireService {

    private final CommentaireMapper mapper;
    private final CommentaireRepo repo;
    private final UserRepo userRepo;
    private final SessionEtudeRepo sessionEtudeRepo;

    public CommentaireService(CommentaireMapper mapper, CommentaireRepo repo, UserRepo userRepo, SessionEtudeRepo sessionEtudeRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
        this.sessionEtudeRepo = sessionEtudeRepo;
    }

    @Transactional
    public CommentaireDto createCommentaire(CommentaireDto dto) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        SessionEtude sessionEtude = sessionEtudeRepo.findById(dto.getSessionEtudeId())
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));

        Commentaire commentaire = mapper.toEntity(dto, user, sessionEtude);
        return mapper.toDto(repo.save(commentaire));
    }

    @Transactional
    public CommentaireDto createCommentaire(CommentaireDto dto, Long currentUserId) {
        dto.setUserId(currentUserId);
        return createCommentaire(dto);
    }

    public CommentaireDto findCommentaireById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Commentaire Not Found"));
    }

    public List<CommentaireDto> findAllCommentaires() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<CommentaireDto> findAllCommentairesByUserId(Long userId) {
        return repo.findCommentaireByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<CommentaireDto> findMyCommentaires(Long currentUserId) {
        return findAllCommentairesByUserId(currentUserId);
    }

    public List<CommentaireDto> findAllCommentairesBySessionEtudeId(Long sessionEtudeId) {
        return repo.findCommentaireBySessionEtudeId(sessionEtudeId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public CommentaireDto updateCommentaireById(CommentaireDto dto, Long id) {
        Commentaire commentaire = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire Not Found"));

        User user = commentaire.getUser();
        if (dto.getUserId() != null) {
            user = userRepo.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User Not Found"));
        }

        SessionEtude sessionEtude = commentaire.getSessionEtude();
        if (dto.getSessionEtudeId() != null) {
            sessionEtude = sessionEtudeRepo.findById(dto.getSessionEtudeId())
                    .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
        }

        mapper.updateEntity(commentaire, dto, user, sessionEtude);
        return mapper.toDto(repo.save(commentaire));
    }

    @Transactional
    public CommentaireDto updateCommentaireById(CommentaireDto dto, Long id, Long currentUserId) {
        Commentaire commentaire = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire Not Found"));
        if (commentaire.getUser() == null || !commentaire.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Commentaire does not belong to current user");
        }
        dto.setUserId(currentUserId);
        return updateCommentaireById(dto, id);
    }

    @Transactional
    public void deleteCommentaireById(Long id) {
        Commentaire commentaire = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire Not Found"));
        repo.delete(commentaire);
    }

    @Transactional
    public void deleteCommentaireById(Long id, Long currentUserId) {
        Commentaire commentaire = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire Not Found"));
        if (commentaire.getUser() == null || !commentaire.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Commentaire does not belong to current user");
        }
        repo.delete(commentaire);
    }
}

