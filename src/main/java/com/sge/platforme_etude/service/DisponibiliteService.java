package com.sge.platforme_etude.service;


import com.sge.platforme_etude.dto.DisponibiliteDto;
import com.sge.platforme_etude.entite.Disponibilite;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.mapper.DisponibiliteMapper;
import com.sge.platforme_etude.repository.DisponibiliteRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class DisponibiliteService {

    private final DisponibiliteMapper mapper;
    private final DisponibiliteRepo repo;
    private final UserRepo userRepo;

    public DisponibiliteService(DisponibiliteMapper mapper, DisponibiliteRepo repo, UserRepo userRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional
    public DisponibiliteDto createDispo(DisponibiliteDto dto , Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(()->new RuntimeException("User Not Found"));

        if (!dto.getHeureDebut().isBefore(dto.getHeureFin())) {
            throw new RuntimeException("heureDebut doit etre avant heureFin");
        }

        Disponibilite disponibilite = mapper.toEntity(dto,user);
        List<Disponibilite> disponibilitesUser = repo.findDisponibiliteByUserId(user.getId());

        for (Disponibilite dispo : disponibilitesUser){

            if(dispo.getJourSemaine().equals(dto.getJourSemaine())
                    //Deux intervalles [start1, end1] et [start2, end2] se chevauchent si : start1 < end2 et start2 < end1
                    && dispo.getHeureDebut().isBefore(dto.getHeureFin())
                    && dto.getHeureDebut().isBefore(dispo.getHeureFin()) ){
                throw new RuntimeException("Impossible d'avoir deux disponibilitees qui se chevauchent");
            }
        }

        Disponibilite saved = repo.save(disponibilite);

        return mapper.toDto(saved);
    }

    public DisponibiliteDto findDispoById(Long id){

        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(()->new RuntimeException("Disponibilite Not Found"));
    }

    public List<DisponibiliteDto> findAllDispo(){

        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<DisponibiliteDto> findAllDispoByUserId(Long userId){

        return repo.findDisponibiliteByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public DisponibiliteDto updateDispoById(DisponibiliteDto dto , Long id , Long userId){
        Disponibilite disponibilite = repo.findById(id)
                .orElseThrow(()->new RuntimeException("Disponibilite Not Found"));

        Long idUser = disponibilite.getUser().getId();

        if (userId != null) {
            if(!idUser.equals(userId)){
                throw new RuntimeException("Cette disponibilite ne vous appartient pas");
            }
        }

        User user = userRepo.findById(idUser).orElseThrow(()->new RuntimeException("User Not Found"));

//        if (dto.getUserId() != null) {
//            user = userRepo.findById(dto.getUserId())
//                    .orElseThrow(() -> new RuntimeException("User Not Found"));
//        }

        //Si le Dto ne contient pas tout les champs
        LocalTime heuredebut = dto.getHeureDebut() != null ? dto.getHeureDebut() : disponibilite.getHeureDebut();
        LocalTime heureFin   = dto.getHeureFin()   != null ? dto.getHeureFin()   : disponibilite.getHeureFin();
        Integer jourSemaine    = dto.getJourSemaine()!= null ? dto.getJourSemaine(): disponibilite.getJourSemaine();

        if (!heuredebut.isBefore(heureFin)) {
            throw new RuntimeException("heureDebut doit etre avant heureFin");
        }

        List<Disponibilite> disponibilitesUser = repo.findDisponibiliteByUserId(user.getId());

        for (Disponibilite dispo : disponibilitesUser){
            if (!dispo.getId().equals(id)) {
                //Verification pour toute les diponibilites sauf la disponibilite qu'on modifie
                if (dispo.getJourSemaine().equals(jourSemaine)
                        //Deux intervalles [start1, end1] et [start2, end2] se chevauchent si : start1 < end2 et start2 < end1
                        && dispo.getHeureDebut().isBefore(heureFin)
                        && heuredebut.isBefore(dispo.getHeureFin())) {
                    throw new RuntimeException("Impossible d'avoir deux disponibilitees qui se chevauchent");
                }
            }
        }

        mapper.updateEntity(disponibilite,dto,user);

        Disponibilite updated = repo.save(disponibilite);

        return mapper.toDto(updated);
    }

    @Transactional
    public void deleteDispoById(Long id , Long userId){
        Disponibilite disponibilite = repo.findById(id)
                .orElseThrow(()->new RuntimeException("Disponibilite Not Found"));

        User user = userRepo.findById(userId).orElseThrow(()-> new RuntimeException("User Not Found"));
        Long idUser = disponibilite.getUser().getId();
        if (userId != null) {
            if(!idUser.equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)){
                throw new RuntimeException("Cette disponibilite ne vous appartient pas");
            }
        }

        repo.delete(disponibilite);
    }



}
