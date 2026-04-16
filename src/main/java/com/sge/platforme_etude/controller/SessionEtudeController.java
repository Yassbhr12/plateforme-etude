package com.sge.platforme_etude.controller;



import com.sge.platforme_etude.dto.SessionEtudeDto;
import com.sge.platforme_etude.service.SessionEtudeService;
import com.sge.platforme_etude.service.user.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/me/sessions")
public class SessionEtudeController {

    private final SessionEtudeService sessionEtudeService;
    private final CurrentUserService currentUserService;

    public SessionEtudeController(SessionEtudeService sessionEtudeService, CurrentUserService currentUserService) {
        this.sessionEtudeService = sessionEtudeService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<SessionEtudeDto> createSessionEtude(@Valid @RequestBody SessionEtudeDto sessionEtudeDto){
        SessionEtudeDto dto = sessionEtudeService.createSessionForCurrentUser(sessionEtudeDto, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/week/regenerate")
    public ResponseEntity<List<SessionEtudeDto>> regenerateWeeklyPlan(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        List<SessionEtudeDto> dtos = sessionEtudeService.regenerateWeeklyPlanForCurrentUser(currentUserService.getCurrentUserId(), effectiveDate);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionEtudeDto> updateSessionEtudeById(@Valid @RequestBody SessionEtudeDto sessionEtudeDto , @PathVariable Long id){
        SessionEtudeDto dto = sessionEtudeService.updateSessionForCurrentUser(id, sessionEtudeDto, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelSessionEtudeById(@PathVariable Long id){
        sessionEtudeService.cancelSessionForCurrentUser(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Session is cancelled successfully");
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<String> markSessionAsDone(@PathVariable Long id){
        sessionEtudeService.markAsDoneForCurrentUser(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Session is marked as done!");
    }
    @GetMapping("/week")
    public ResponseEntity<List<SessionEtudeDto>> getSessionsByWeek(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        LocalDate effectiveDate = (date != null) ? date : LocalDate.now();
        List<SessionEtudeDto> dtos = sessionEtudeService.getSessionsByWeekForCurrentUser(currentUserService.getCurrentUserId(), effectiveDate);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping("/day")
    public ResponseEntity<List<SessionEtudeDto>> getSessionByDay(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        List<SessionEtudeDto> dtos = sessionEtudeService.getSessionByDayForCurrentUser(currentUserService.getCurrentUserId(), date);
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @GetMapping
    public ResponseEntity<List<SessionEtudeDto>> getMySessions() {
        return ResponseEntity.status(HttpStatus.OK).body(sessionEtudeService.findMySessions(currentUserService.getCurrentUserId()));
    }

    @PatchMapping("/{id}/share")
    public ResponseEntity<SessionEtudeDto> shareSessionInGroup(
            @PathVariable Long id,
            @RequestParam Long groupeEtudeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(sessionEtudeService.partagerSessionDansGroupe(id, groupeEtudeId, currentUserService.getCurrentUserId()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSessionById(@PathVariable Long id){
        sessionEtudeService.deleteSessionEtudeByIdForCurrentUser(id, currentUserService.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Session is deleted successfully");

    }

}
