package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.ObjectifHebdoDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.ObjectifHebdo;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.mapper.ObjectifHebdoMapper;
import com.sge.platforme_etude.repository.MatiereRepo;
import com.sge.platforme_etude.repository.ObjectifHebdoRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

@Service
public class ObjectifHebdoService {

    private final ObjectifHebdoMapper mapper;
    private final ObjectifHebdoRepo repo;
    private final UserRepo userRepo;
    private final MatiereRepo matiereRepo;

    public ObjectifHebdoService(ObjectifHebdoMapper mapper, ObjectifHebdoRepo repo, UserRepo userRepo, MatiereRepo matiereRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
        this.matiereRepo = matiereRepo;
    }

    @Transactional
    public ObjectifHebdoDto createObjectifHebdo(ObjectifHebdoDto dto , Long userId) {
        validateRequiredFields(dto);
        LocalDate monday = normalizeToMonday(dto.getSemaine());

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        Matiere matiere = matiereRepo.findById(dto.getMatiereId())
                .orElseThrow(() -> new RuntimeException("Matiere Not Found"));

        validateMatiereOwnership(matiere, user);

        if (repo.existsByUserIdAndMatiereIdAndSemaine(user.getId(), matiere.getId(), monday)) {
            throw new RuntimeException("ObjectifHebdo already exists for this user, matiere and semaine");
        }

        ObjectifHebdo objectifHebdo = mapper.toEntity(dto, user, matiere);
        objectifHebdo.setSemaine(monday);
        return mapper.toDto(repo.save(objectifHebdo));
    }

    public List<ObjectifHebdoDto> findObjectifByUserIdAndSemaine(Long userId , LocalDate date){
        User user = userRepo.findById(userId)
                .orElseThrow(()->new RuntimeException("User Not Found"));
        LocalDate monday = normalizeToMonday(date);

        return repo.findByUserIdAndSemaine(userId,monday)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ObjectifHebdoDto findObjectifHebdoById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("ObjectifHebdo Not Found"));
    }

    public List<ObjectifHebdoDto> findAllObjectifsHebdo() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ObjectifHebdoDto> findAllObjectifsHebdoByUserId(Long userId) {
        return repo.findObjectifHebdoByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ObjectifHebdoDto> findAllObjectifsHebdoByMatiereId(Long matiereId) {
        return repo.findObjectifHebdoByMatiereId(matiereId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<ObjectifHebdoDto> findAllObjectifsHebdoByUserIdAndMatiereId(Long matiereId , Long userId){
        return repo.findObjectifHebdoByUserIdAndMatiereId(userId,matiereId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public ObjectifHebdoDto updateObjectifHebdoById(ObjectifHebdoDto dto, Long id , Long userId) {
        ObjectifHebdo objectifHebdo = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("ObjectifHebdo Not Found"));

        User user = userRepo.findById(userId)
                .orElseThrow(()->new RuntimeException("User Not Found"));
        Long idUser = objectifHebdo.getUser().getId();
        if (!idUser.equals(userId)) {
            throw new RuntimeException("Cette objectif ne vous appartient pas");
        }

        Matiere matiere = objectifHebdo.getMatiere();
        if (dto.getMatiereId() != null) {
            matiere = matiereRepo.findById(dto.getMatiereId())
                    .orElseThrow(() -> new RuntimeException("Matiere Not Found"));
        }

        validateMatiereOwnership(matiere, user);

        LocalDate semaine = dto.getSemaine() != null ? dto.getSemaine() : objectifHebdo.getSemaine();
        if (semaine == null) {
            throw new RuntimeException("Semaine is required");
        }
        LocalDate monday = normalizeToMonday(semaine);

        repo.findByUserIdAndMatiereIdAndSemaine(user.getId(), matiere.getId(), monday)
                .ifPresent(existing -> {
                    if (!Objects.equals(existing.getId(), objectifHebdo.getId())) {
                        throw new RuntimeException("ObjectifHebdo already exists for this user, matiere and semaine");
                    }
                });

        mapper.updateEntity(objectifHebdo, dto, user, matiere);
        objectifHebdo.setSemaine(monday);
        return mapper.toDto(repo.save(objectifHebdo));
    }

    @Transactional
    public void deleteObjectifHebdoById(Long id , Long userId) {
        ObjectifHebdo objectifHebdo = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("ObjectifHebdo Not Found"));

        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User Not Found"));
        Long idUser = objectifHebdo.getUser().getId();
        if (userId != null) {
            if(!idUser.equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)){
                throw new RuntimeException("Cette objectif ne vous appartient pas");
            }
        }
        repo.delete(objectifHebdo);
    }

    private void validateRequiredFields(ObjectifHebdoDto dto) {
//        if (dto.getUserId() == null) {
//            throw new RuntimeException("User id is required");
//        }
        if (dto.getMatiereId() == null) {
            throw new RuntimeException("Matiere id is required");
        }
        if (dto.getSemaine() == null) {
            throw new RuntimeException("Semaine is required");
        }
    }

    private void validateMatiereOwnership(Matiere matiere, User user) {
        if (matiere.getUser() == null || !Objects.equals(matiere.getUser().getId(), user.getId())) {
            throw new RuntimeException("Matiere does not belong to the user");
        }
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}

