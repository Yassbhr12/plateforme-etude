package com.sge.platforme_etude.service;

import com.sge.platforme_etude.dto.SessionEtudeDto;
import com.sge.platforme_etude.entite.*;
import com.sge.platforme_etude.helper.enums.StatutSession;
import com.sge.platforme_etude.mapper.SessionEtudeMapper;
import com.sge.platforme_etude.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


@Service
public class SessionEtudeService {

    private final SessionEtudeMapper mapper;
    private final SessionEtudeRepo repo;
    private final UserRepo userRepo;
    private final MatiereRepo matiereRepo;
    private final GroupeEtudeRepo groupeEtudeRepo;
    private final ObjectifHebdoRepo objectifHebdoRepo;
    private final DisponibiliteRepo disponibiliteRepo;

    public SessionEtudeService(SessionEtudeMapper mapper, SessionEtudeRepo repo, UserRepo userRepo, MatiereRepo matiereRepo, GroupeEtudeRepo groupeEtudeRepo, ObjectifHebdoRepo objectifHebdoRepo, DisponibiliteRepo disponibiliteRepo) {
        this.mapper = mapper;
        this.repo = repo;
        this.userRepo = userRepo;
        this.matiereRepo = matiereRepo;
        this.groupeEtudeRepo = groupeEtudeRepo;
        this.objectifHebdoRepo = objectifHebdoRepo;
        this.disponibiliteRepo = disponibiliteRepo;
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDateTime startOfWeek(LocalDate monday) {
        return monday.atStartOfDay();
    }

    private LocalDateTime endOfWeekExclusive(LocalDate monday) {
        return monday.plusDays(7).atStartOfDay();
    }

    //Deux intervalles [start1, end1] et [start2, end2] se chevauchent si : start1 < end2 et start2 < end1
    private boolean overlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private void assertNoOverlap(Long userId, LocalDateTime start, LocalDateTime end, Long excludeSessionId) {

        LocalDate day = start.toLocalDate();
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

        List<SessionEtude> sessions =
                repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(userId, dayStart, dayEnd);

        for (SessionEtude s : sessions) {
            if (s.getStatut() == StatutSession.ANNULEE) continue;
            if (excludeSessionId != null && s.getId().equals(excludeSessionId)) continue;
            if (overlap(start, end, s.getDateDebut(), s.getDateFin())) {
                throw new RuntimeException("Session overlaps with existing session id=" + s.getId());
            }
        }
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }
    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static class TimeSlot {
        LocalDateTime start;
        LocalDateTime end;

        TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        long minutes() {
            return java.time.Duration.between(start, end).toMinutes();
        }
    }

    private List<TimeSlot> toSlots(List<Disponibilite> dispos, LocalDate weekMonday) {
        List<TimeSlot> slots = new ArrayList<>();
        for (Disponibilite d : dispos) {
            LocalDate date = weekMonday.plusDays(d.getJourSemaine() - 1);
            LocalDateTime start = LocalDateTime.of(date, d.getHeureDebut());
            LocalDateTime end = LocalDateTime.of(date, d.getHeureFin());
            if (start.isBefore(end)) {
                slots.add(new TimeSlot(start, end));
            }
        }
        slots.sort(Comparator.comparing(s -> s.start));
        return slots;
    }

    //Soustraire l'intervalle occupe par une session d'un TimeSlot pour obtenir les nouveaux creneaux libres restants.
    private List<TimeSlot> subtract(TimeSlot slot , SessionEtude sessionEtude){

        //Cas 1 : Si pas de chevauchement , la session n'occupe rien du TimeSlot , donc on ne change rien
        if(!overlap(slot.start , slot.end , sessionEtude.getDateDebut() , sessionEtude.getDateFin())){
            return List.of(slot);
        }

        //Cas 2 : Si la session couvre completement le TimeSlot ,
        if( (sessionEtude.getDateDebut().isBefore(slot.start) || sessionEtude.getDateDebut().equals(slot.start) )
                && ( sessionEtude.getDateFin().isAfter(slot.end) || sessionEtude.getDateFin().equals(slot.end)  ) ){
            return List.of();
        }

        //Cas 3 : chevauchement partiel
        List<TimeSlot> result = new ArrayList<>();


        //Partie gauche
        if(slot.start.isBefore(sessionEtude.getDateDebut())){
            result.add(new TimeSlot(slot.start , min(slot.end,sessionEtude.getDateDebut() )) );
        }

        //Partie droite
        if (slot.end.isAfter(sessionEtude.getDateFin())) {
            result.add(new TimeSlot(max(slot.start, sessionEtude.getDateFin()), slot.end));
        }

        // filtrer les slots vides ou negatifs
        result.removeIf(t -> !t.start.isBefore(t.end));
        return result;
    }


    private List<TimeSlot> computeFreeSlots(List<TimeSlot> slots, List<SessionEtude> doneSessions) {
        List<TimeSlot> free = new ArrayList<>(slots);
        for (SessionEtude s : doneSessions) {
            List<TimeSlot> next = new ArrayList<>();
            for (TimeSlot t : free) {
                next.addAll(subtract(t, s));
            }
            free = next;
            free.sort(Comparator.comparing(x -> x.start));
        }
        return free;
    }

    private Map<Long, Long> doneMinutesByMatiere(List<SessionEtude> doneSessions) {
        Map<Long, Long> map = new HashMap<>();
        for (SessionEtude s : doneSessions) {
            Long matiereId = s.getMatiere().getId();
            long minutes = Duration.between(s.getDateDebut(), s.getDateFin()).toMinutes();
            map.merge(matiereId, minutes, Long::sum);
        }
        return map;
    }

    private List<SessionEtude> allocateSessions(
            User user,
            LocalDate weekMonday,
            List<ObjectifHebdo> objectifs,
            List<TimeSlot> freeSlots,
            List<SessionEtude> doneSessions
    ) {
        final int MAX_SESSION_MINUTES = 60;
        final int MIN_SESSION_MINUTES = 15;

        Map<Long, Long> doneByMatiere = doneMinutesByMatiere(doneSessions);

        objectifs.sort(Comparator.comparingInt(o -> o.getMatiere().getPriorite()));

        List<SessionEtude> generated = new ArrayList<>();

        for (ObjectifHebdo obj : objectifs) {
            Matiere matiere = obj.getMatiere();

            long targetMinutes = obj.getHeuresCibles() * 60L;
            long done = doneByMatiere.getOrDefault(matiere.getId(), 0L);
            long remaining = targetMinutes - done;

            while (remaining > 0 && !freeSlots.isEmpty()) {
                TimeSlot slot = freeSlots.get(0);

                long slotMinutes = slot.minutes();
                long sessionMinutes = Math.min(remaining, Math.min(MAX_SESSION_MINUTES, slotMinutes));

                if (sessionMinutes < MIN_SESSION_MINUTES) {
                    // slot trop petit, on l'abandonne
                    freeSlots.remove(0);
                    continue;
                }

                LocalDateTime start = slot.start;
                LocalDateTime end = start.plusMinutes(sessionMinutes);

                SessionEtude s = new SessionEtude();
                s.setTitre("Etude " + matiere.getNom());
                s.setDateDebut(start);
                s.setDateFin(end);
                s.setDureeMax((int) sessionMinutes);
                s.setStatut(StatutSession.PLANIFIEE);
                s.setPrivee(true);
                s.setUser(user);
                s.setMatiere(matiere);
                s.setGroupeEtude(null);

                generated.add(s);

                // consommer le slot
                slot.start = end;
                if (!slot.start.isBefore(slot.end)) {
                    freeSlots.remove(0);
                }

                remaining -= sessionMinutes;
            }
        }

        return generated;
    }

    private SessionEtude getOwnedSessionOrThrow(Long sessionId, Long currentUserId) {
        SessionEtude sessionEtude = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
        if (sessionEtude.getUser() == null || !sessionEtude.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("SessionEtude does not belong to current user");
        }
        return sessionEtude;
    }


    @Transactional
    public List<SessionEtudeDto> regenerateWeeklyPlan(Long userId , LocalDate anyDateOfWeek){
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        LocalDate monday = normalizeToMonday(anyDateOfWeek);
        LocalDateTime start = startOfWeek(monday);
        LocalDateTime end = endOfWeekExclusive(monday);

        //supprimer les sessions deja planifiee de la semaine
        repo.deleteByUserIdAndStatutAndDateDebutGreaterThanEqualAndDateDebutLessThan(
                userId, StatutSession.PLANIFIEE, start, end
        );

        /*
        Extraire tous les sessions d'etude d'un utilisateur(avec userId) dans une semaine donnee
            par le debut qui est le lundi a 00:00 et une fin qui le lundi prochain a 00:00 mais exclusive
        */
        List<SessionEtude> sessionEtudes = repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(userId,start,end);

        //Extraire a partir du liste precedente tous les sessions d'etudes qui ont le statut TERMINEE
        List<SessionEtude> doneSessionEtudes = sessionEtudes.stream()
                .filter(s->s.getStatut() == StatutSession.TERMINEE)
                .toList();

        List<ObjectifHebdo> objectifHebdos = objectifHebdoRepo.findByUserIdAndSemaine(userId,monday);

        //Recuperer les disponibilites de l'utilisateur puis on les convertit en TimeSlot (des intervalles du temps)
        List<Disponibilite> disponibilites = disponibiliteRepo.findDisponibiliteByUserId(userId);
        List<TimeSlot> slots = toSlots(disponibilites,monday);


        List<TimeSlot> freeSlots = computeFreeSlots(slots, doneSessionEtudes);

        List<SessionEtude> generated = allocateSessions(user, monday, objectifHebdos, freeSlots, doneSessionEtudes);

        List<SessionEtude> saved = repo.saveAll(generated);

        return mapper.toDtoList(saved);
    }

    @Transactional
    public List<SessionEtudeDto> regenerateWeeklyPlanForCurrentUser(Long currentUserId, LocalDate anyDateOfWeek) {
        return regenerateWeeklyPlan(currentUserId, anyDateOfWeek);
    }


    @Transactional
    public SessionEtudeDto createSession(SessionEtudeDto dto) {
        if (dto.getUserId() == null) throw new RuntimeException("userId is required");
        if (dto.getMatiereId() == null) throw new RuntimeException("matiereId is required");
        if (dto.getDateDebut() == null || dto.getDateFin() == null) throw new RuntimeException("dates are required");
        if (!dto.getDateDebut().isBefore(dto.getDateFin())) throw new RuntimeException("dateDebut must be before dateFin");

        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User Not Found"));
        Matiere matiere = matiereRepo.findById(dto.getMatiereId()).orElseThrow(() -> new RuntimeException("Matiere Not Found"));

        long minutes = Duration.between(dto.getDateDebut(), dto.getDateFin()).toMinutes();
        if (minutes <= 0) throw new RuntimeException("Invalid session duration");
        if (minutes > 180) throw new RuntimeException("Session duration must be <= 180 minutes");

        // Chevauchement avec sessions non annulees
        assertNoOverlap(user.getId(), dto.getDateDebut(), dto.getDateFin(), null);

        SessionEtude s = new SessionEtude();
        s.setTitre(dto.getTitre() == null ? ("Etude " + matiere.getNom()) : dto.getTitre());
        s.setDateDebut(dto.getDateDebut());
        s.setDateFin(dto.getDateFin());
        s.setDureeMax(dto.getDureeMax() == null ? (int) minutes : dto.getDureeMax());
        s.setStatut(dto.getStatut() == null ? StatutSession.PLANIFIEE : dto.getStatut());
        s.setPrivee(dto.getPrivee() == null ? true : dto.getPrivee());
        s.setUser(user);
        s.setMatiere(matiere);

        if (dto.getGroupeEtudeId() != null) {
            GroupeEtude g = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                    .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
            s.setGroupeEtude(g);
        } else {
            s.setGroupeEtude(null);
        }

        return mapper.toDto(repo.save(s));
    }

    @Transactional
    public SessionEtudeDto createSessionForCurrentUser(SessionEtudeDto dto, Long currentUserId) {
        dto.setUserId(currentUserId);
        return createSession(dto);
    }

    @Transactional
    public SessionEtudeDto updateSession(Long sessionId, SessionEtudeDto dto) {
        SessionEtude s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session Not Found"));


        if (s.getStatut() == StatutSession.TERMINEE &&
                (dto.getDateDebut() != null || dto.getDateFin() != null)) {
            throw new RuntimeException("Cannot change dates for a done session");
        }

        if (dto.getTitre() != null) s.setTitre(dto.getTitre());
        if (dto.getPrivee() != null) s.setPrivee(dto.getPrivee());

        if (dto.getMatiereId() != null) {
            Matiere matiere = matiereRepo.findById(dto.getMatiereId())
                    .orElseThrow(() -> new RuntimeException("Matiere Not Found"));
            s.setMatiere(matiere);
        }


        LocalDateTime newStart = dto.getDateDebut() != null ? dto.getDateDebut() : s.getDateDebut();
        LocalDateTime newEnd = dto.getDateFin() != null ? dto.getDateFin() : s.getDateFin();
        if (!newStart.isBefore(newEnd)) throw new RuntimeException("dateDebut must be before dateFin");

        if (dto.getDateDebut() != null || dto.getDateFin() != null) {
            assertNoOverlap(s.getUser().getId(), newStart, newEnd, s.getId());
            s.setDateDebut(newStart);
            s.setDateFin(newEnd);

            long minutes = Duration.between(newStart, newEnd).toMinutes();
            if (minutes > 180) throw new RuntimeException("Session duration must be <= 180 minutes");
            if (dto.getDureeMax() != null) s.setDureeMax(dto.getDureeMax());
            else s.setDureeMax((int) minutes);
        } else if (dto.getDureeMax() != null) {
            s.setDureeMax(dto.getDureeMax());
        }

        if (dto.getStatut() != null) {
            s.setStatut(dto.getStatut());
        }

        if (dto.getGroupeEtudeId() != null) {
            GroupeEtude g = groupeEtudeRepo.findById(dto.getGroupeEtudeId())
                    .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));
            s.setGroupeEtude(g);
        }

        return mapper.toDto(repo.save(s));
    }

    @Transactional
    public SessionEtudeDto updateSessionForCurrentUser(Long sessionId, SessionEtudeDto dto, Long currentUserId) {
        getOwnedSessionOrThrow(sessionId, currentUserId);
        dto.setUserId(currentUserId);
        return updateSession(sessionId, dto);
    }

    @Transactional
    public void cancelSession(Long sessionId) {
        SessionEtude s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
        s.setStatut(StatutSession.ANNULEE);
        repo.save(s);
    }

    @Transactional
    public void cancelSessionForCurrentUser(Long sessionId, Long currentUserId) {
        SessionEtude s = getOwnedSessionOrThrow(sessionId, currentUserId);
        s.setStatut(StatutSession.ANNULEE);
        repo.save(s);
    }

    @Transactional
    public void markAsDone(Long sessionId) {
        SessionEtude s = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
        if (s.getStatut() != StatutSession.ANNULEE) {
            s.setStatut(StatutSession.TERMINEE);
            repo.save(s);
        }
    }

    @Transactional
    public void markAsDoneForCurrentUser(Long sessionId, Long currentUserId) {
        SessionEtude s = getOwnedSessionOrThrow(sessionId, currentUserId);
        if (s.getStatut() != StatutSession.ANNULEE) {
            s.setStatut(StatutSession.TERMINEE);
            repo.save(s);
        }
    }


    public SessionEtudeDto findSessionEtudeById(Long id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
    }

    public List<SessionEtudeDto> findAllSessionsEtude() {
        return repo.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<SessionEtudeDto> getSessionsByWeek(Long userId, LocalDate anyDateInWeek) {
        LocalDate weekMonday = normalizeToMonday(anyDateInWeek);
        LocalDateTime start = startOfWeek(weekMonday);
        LocalDateTime end = endOfWeekExclusive(weekMonday);

        List<SessionEtude> sessions =
                repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(userId, start, end);

        return mapper.toDtoList(sessions);
    }

    public List<SessionEtudeDto> getSessionsByWeekForCurrentUser(Long currentUserId, LocalDate anyDateInWeek) {
        return getSessionsByWeek(currentUserId, anyDateInWeek);
    }

    public List<SessionEtudeDto> getSessionByDay(Long userId , LocalDate date){

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<SessionEtude> sessionEtudes =
                repo.findByUserIdAndDateDebutGreaterThanEqualAndDateDebutLessThan(userId,start,end);
        return mapper.toDtoList(sessionEtudes);

    }

    public List<SessionEtudeDto> getSessionByDayForCurrentUser(Long currentUserId, LocalDate date) {
        return getSessionByDay(currentUserId, date);
    }

    public List<SessionEtudeDto> findAllSessionsEtudeByUserId(Long userId) {
        return repo.findSessionEtudeByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<SessionEtudeDto> findMySessions(Long currentUserId) {
        return findAllSessionsEtudeByUserId(currentUserId);
    }

    public List<SessionEtudeDto> findAllSessionsEtudeByMatiereId(Long matiereId) {
        return repo.findSessionEtudeByMatiereId(matiereId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<SessionEtudeDto> findAllSessionsEtudeByGroupeEtudeId(Long groupeEtudeId) {
        return repo.findSessionEtudeByGroupeEtudeId(groupeEtudeId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }


    @Transactional
    public SessionEtudeDto partagerSessionDansGroupe(Long sessionId, Long groupeEtudeId, Long userId) {
        SessionEtude sessionEtude = repo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));

        if (sessionEtude.getUser() == null || !sessionEtude.getUser().getId().equals(userId)) {
            throw new RuntimeException("Only the session owner can share it");
        }

        GroupeEtude groupeEtude = groupeEtudeRepo.findById(groupeEtudeId)
                .orElseThrow(() -> new RuntimeException("GroupeEtude Not Found"));

        sessionEtude.setPrivee(false);
        sessionEtude.setGroupeEtude(groupeEtude);

        return mapper.toDto(repo.save(sessionEtude));
    }

    @Transactional
    public void deleteSessionEtudeById(Long id) {
        SessionEtude sessionEtude = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("SessionEtude Not Found"));
        repo.delete(sessionEtude);
    }

    @Transactional
    public void deleteSessionEtudeByIdForCurrentUser(Long id, Long currentUserId) {
        SessionEtude sessionEtude = getOwnedSessionOrThrow(id, currentUserId);
        repo.delete(sessionEtude);
    }
}
