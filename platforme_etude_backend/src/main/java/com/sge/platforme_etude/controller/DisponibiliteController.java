package com.sge.platforme_etude.controller;

import com.sge.platforme_etude.dto.DisponibiliteDto;
import com.sge.platforme_etude.service.DisponibiliteService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/disponibilites")
@RequiredArgsConstructor
public class DisponibiliteController {

    private final DisponibiliteService service;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<DisponibiliteDto>> getDisponibilitesByUserId(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAllDispoByUserId(currentUserService.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<DisponibiliteDto> createDisponibilite(@Valid @RequestBody DisponibiliteDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDispo(dto , currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisponibiliteDto> updateDisponibiliteById(@PathVariable Long id , @Valid @RequestBody DisponibiliteDto dto){
        return ResponseEntity.status(HttpStatus.OK).body(service.updateDispoById(dto,id , currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDisponibilitesById(@PathVariable Long id){
        service.deleteDispoById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Disponibilite is deleted successfully");
    }
}
