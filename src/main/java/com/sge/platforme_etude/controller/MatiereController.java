package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.MatiereDto;
import com.sge.platforme_etude.service.MatiereService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/matieres")
@RequiredArgsConstructor
public class MatiereController {

    private final MatiereService matiereService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<MatiereDto>> getMatieresByUserId(){
        return ResponseEntity.status(HttpStatus.OK).body(matiereService.findAllMatieresByUserId(currentUserService.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<MatiereDto> createMatiere(@Valid @RequestBody MatiereDto dto){
        MatiereDto matiereDto = matiereService.createMatiere(dto , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(matiereDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatiereDto> updateMatiereById(@Valid @RequestBody MatiereDto dto , @PathVariable Long id){
        MatiereDto matiereDto = matiereService.updateMatiereById(dto,id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(matiereDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMatiereById(@PathVariable Long id){
        matiereService.deleteMatiereById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("La matiere aved l'id "+ id + " a ete supprime avec succes");
    }
}
