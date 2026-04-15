package com.sge.platforme_etude.service;


import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.entite.Matiere;
import com.sge.platforme_etude.entite.User;
import com.sge.platforme_etude.helper.enums.Role;
import com.sge.platforme_etude.mapper.MatiereMapper;
import com.sge.platforme_etude.repository.MatiereRepo;
import com.sge.platforme_etude.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class MatiereService {

    private final MatiereMapper mapper;
    private final MatiereRepo repo;
    private final UserRepo userRepo;

    public MatiereService(MatiereMapper mapper, MatiereRepo repo, UserRepo userRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional
    public MatiereDto createMatiere(MatiereDto dto,Long userId){

        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found")) ;
        Matiere matiere = mapper.toEntity(dto,user);
        Matiere saved = repo.save(matiere);

        return mapper.toDto(saved);
    }

    public MatiereDto findMatiereById(Long id){
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(()-> new RuntimeException("Matiere Not Found"));
    }

    public List<MatiereDto> findAllMatieres(){

        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<MatiereDto> findAllMatieresByUserId(Long userId){

        return repo.findMatiereByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public MatiereDto updateMatiereById(MatiereDto dto , Long id , Long userId){

        Matiere matiere = repo.findById(id).orElseThrow(()-> new RuntimeException("Matiere Not Found"));

        Long idUser = matiere.getUser().getId();
        if (userId != null) {
            if(!idUser.equals(userId)){
                throw new RuntimeException("Cette matiere ne vous appartient pas");
            }
        }

        User user = userRepo.findById(idUser).orElseThrow(()->new RuntimeException("User Not Found"));

        mapper.updateEntity(matiere,dto,user);
        Matiere updated = repo.save(matiere);

        return mapper.toDto(updated);
    }

    @Transactional
    public void deleteMatiereById(Long id , Long userId){

        Matiere matiere = repo.findById(id).orElseThrow(()->new RuntimeException("Matiere Not Found"));
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("User Not Found"));

        Long idUser = matiere.getUser().getId();
        if (!idUser.equals(userId) && !user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("Cette matiere ne vous appartient pas");
        }

        repo.delete(matiere);
    }

}
