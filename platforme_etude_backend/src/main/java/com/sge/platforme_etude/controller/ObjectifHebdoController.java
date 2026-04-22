package com.sge.platforme_etude.controller;


import com.sge.platforme_etude.dto.ObjectifHebdoDto;

import com.sge.platforme_etude.service.ObjectifHebdoService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ObjectifHebdoController {

    private final ObjectifHebdoService objectifHebdoService;
    private final CurrentUserService currentUserService;


    @PostMapping("/objectifs")
    public ResponseEntity<ObjectifHebdoDto> createObjectifHedo(@Valid @RequestBody ObjectifHebdoDto dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(objectifHebdoService.createObjectifHebdo(dto , currentUserService.getCurrentUserId()));
    }

    @PutMapping("/objectifs/{id}")
    public ResponseEntity<ObjectifHebdoDto> updateObjectifsHebdoById(@PathVariable Long id , @Valid @RequestBody ObjectifHebdoDto dto){
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.updateObjectifHebdoById(dto,id , currentUserService.getCurrentUserId()));
    }

    @GetMapping("/objectifs")
    public ResponseEntity<List<ObjectifHebdoDto>> getObjectifsHebdoByUserId(){
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.findAllObjectifsHebdoByUserId(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/objectifs/week")
    public ResponseEntity<List<ObjectifHebdoDto>> getObjectifHebdoByUserIdAndWeek(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        List<ObjectifHebdoDto> dtos = objectifHebdoService.findObjectifByUserIdAndSemaine(currentUserService.getCurrentUserId(), effectiveDate);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/matieres/{matiereId}/objectifs")
    public ResponseEntity<List<ObjectifHebdoDto>> getObjectifsHebdoByMatiereId(@PathVariable Long matiereId) {
        return ResponseEntity.status(HttpStatus.OK).body(objectifHebdoService.findAllObjectifsHebdoByUserIdAndMatiereId(matiereId , currentUserService.getCurrentUserId()));
    }

    @DeleteMapping("/objectifs/{id}")
    public ResponseEntity<String> deleteObjectifHebdoById(@PathVariable Long id){
        objectifHebdoService.deleteObjectifHebdoById(id , currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Objectif is deleted successfully");
    }

}
