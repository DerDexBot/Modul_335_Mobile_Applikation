package com.workforce.planning.service;

import com.workforce.planning.dto.CreateShiftRequest;
import com.workforce.planning.dto.CreateWorkPlanRequest;
import com.workforce.planning.dto.ShiftResponse;
import com.workforce.planning.dto.UpdateWorkPlanRequest;
import com.workforce.planning.dto.WorkPlanResponse;
import com.workforce.planning.exception.ResourceNotFoundException;
import com.workforce.planning.model.Shift;
import com.workforce.planning.model.WorkPlan;
import com.workforce.planning.model.WorkPlanStatus;
import com.workforce.planning.repository.ShiftRepository;
import com.workforce.planning.repository.WorkPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Service-Klasse für Arbeitspläne und Schichten.
 *
 * <p>Implementiert die besprochenen Kernfunktionen:
 * Arbeitsplan erstellen, Schichten hinzufügen, geplante Stunden berechnen,
 * Warnungen bei Über-/Unterplanung und Mitarbeiter-Kalender bereitstellen.</p>
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private static final BigDecimal UNDER_PLANNED_FACTOR = BigDecimal.valueOf(0.95);

    private final WorkPlanRepository workPlanRepository;
    private final ShiftRepository shiftRepository;

    /** Erstellt einen neuen Arbeitsplan mit HR-Stundenkontingent. */
    @Transactional
    public WorkPlanResponse createWorkPlan(CreateWorkPlanRequest request) {
        validateCreateWorkPlanRequest(request);

        WorkPlan workPlan = new WorkPlan();
        workPlan.setTitle(request.title().trim());
        workPlan.setShiftLeadId(request.shiftLeadId());
        workPlan.setStartDate(request.startDate());
        workPlan.setEndDate(request.endDate());
        workPlan.setApprovedHours(normalizeHours(request.approvedHours()));
        workPlan.setStatus(WorkPlanStatus.DRAFT);

        WorkPlan saved = workPlanRepository.save(workPlan);
        return toResponse(saved);
    }

    /** Gibt alle Arbeitspläne zurück, optional gefiltert nach Schichtleiter. */
    @Transactional(readOnly = true)
    public List<WorkPlanResponse> getWorkPlans(Long shiftLeadId) {
        List<WorkPlan> workPlans = shiftLeadId != null
                ? workPlanRepository.findByShiftLeadIdOrderByStartDateDesc(shiftLeadId)
                : workPlanRepository.findAllByOrderByStartDateDesc();

        return workPlans.stream().map(this::toResponse).toList();
    }

    /** Gibt einen Arbeitsplan inklusive Schichten und Stundenübersicht zurück. */
    @Transactional(readOnly = true)
    public WorkPlanResponse getWorkPlan(Long id) {
        WorkPlan workPlan = findWorkPlan(id);
        return toResponse(workPlan);
    }

    /** Bearbeitet Metadaten eines Arbeitsplan-Entwurfs. */
    @Transactional
    public WorkPlanResponse updateWorkPlan(Long workPlanId, UpdateWorkPlanRequest request) {
        WorkPlan workPlan = findWorkPlan(workPlanId);
        ensureDraft(workPlan);
        validateUpdateWorkPlanRequest(request);

        workPlan.setTitle(request.title().trim());
        workPlan.setStartDate(request.startDate());
        workPlan.setEndDate(request.endDate());
        workPlan.setApprovedHours(normalizeHours(request.approvedHours()));

        validateExistingShiftsStillFit(workPlan);
        return toResponse(workPlanRepository.save(workPlan));
    }

    /** Fügt einem Arbeitsplan eine neue Schicht hinzu. */
    @Transactional
    public WorkPlanResponse addShift(Long workPlanId, CreateShiftRequest request) {
        WorkPlan workPlan = findWorkPlan(workPlanId);
        ensureDraft(workPlan);
        validateCreateShiftRequest(workPlan, request);
        validateNoOverlap(request);

        Shift shift = new Shift();
        shift.setWorkPlan(workPlan);
        shift.setEmployeeId(request.employeeId());
        shift.setOrderId(request.orderId());
        shift.setShiftDate(request.shiftDate());
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setNotes(normalizeText(request.notes()));

        shiftRepository.save(shift);
        return toResponse(workPlan);
    }

    /** Veröffentlicht einen Arbeitsplan, damit Mitarbeiter ihn im Kalender sehen. */
    @Transactional
    public WorkPlanResponse publishWorkPlan(Long workPlanId) {
        WorkPlan workPlan = findWorkPlan(workPlanId);
        List<Shift> shifts = shiftRepository.findByWorkPlanIdOrderByShiftDateAscStartTimeAsc(workPlanId);

        if (shifts.isEmpty()) {
            throw new IllegalStateException("Arbeitsplan kann ohne Schichten nicht veröffentlicht werden");
        }

        workPlan.setStatus(WorkPlanStatus.PUBLISHED);
        workPlan.setPublishedAt(LocalDateTime.now());
        WorkPlan saved = workPlanRepository.save(workPlan);

        return toResponse(saved);
    }

    /** Gibt veröffentlichte Kalenderschichten eines Mitarbeiters zurück. */
    @Transactional(readOnly = true)
    public List<ShiftResponse> getEmployeeCalendar(Long employeeId, LocalDate from, LocalDate to) {
        if (employeeId == null) {
            throw new IllegalArgumentException("employeeId ist erforderlich");
        }

        LocalDate start = from != null ? from : YearMonth.now().atDay(1);
        LocalDate end = to != null ? to : YearMonth.now().atEndOfMonth();

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("to darf nicht vor from liegen");
        }

        return shiftRepository.findCalendarShifts(employeeId, start, end, WorkPlanStatus.PUBLISHED)
                .stream()
                .map(shift -> ShiftResponse.from(shift, calculateShiftHours(shift)))
                .toList();
    }

    private WorkPlan findWorkPlan(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("workPlanId ist erforderlich");
        }
        return workPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Arbeitsplan " + id + " wurde nicht gefunden"));
    }

    private WorkPlanResponse toResponse(WorkPlan workPlan) {
        List<Shift> shifts = shiftRepository.findByWorkPlanIdOrderByShiftDateAscStartTimeAsc(workPlan.getId());
        List<ShiftResponse> shiftResponses = shifts.stream()
                .map(shift -> ShiftResponse.from(shift, calculateShiftHours(shift)))
                .toList();

        BigDecimal plannedHours = shifts.stream()
                .map(this::calculateShiftHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal approvedHours = normalizeHours(workPlan.getApprovedHours());
        BigDecimal remainingHours = approvedHours.subtract(plannedHours).setScale(2, RoundingMode.HALF_UP);
        boolean overLimit = approvedHours.compareTo(BigDecimal.ZERO) > 0 && plannedHours.compareTo(approvedHours) > 0;
        boolean underPlanned = approvedHours.compareTo(BigDecimal.ZERO) > 0
                && plannedHours.compareTo(approvedHours.multiply(UNDER_PLANNED_FACTOR)) < 0;

        return WorkPlanResponse.from(
                workPlan,
                plannedHours,
                remainingHours,
                overLimit,
                underPlanned,
                shiftResponses
        );
    }

    private BigDecimal calculateShiftHours(Shift shift) {
        long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        if (minutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private void validateCreateWorkPlanRequest(CreateWorkPlanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request darf nicht leer sein");
        }
        if (request.shiftLeadId() == null) {
            throw new IllegalArgumentException("shiftLeadId ist erforderlich");
        }
        validateWorkPlanFields(request.title(), request.startDate(), request.endDate(), request.approvedHours());
    }

    private void validateUpdateWorkPlanRequest(UpdateWorkPlanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request darf nicht leer sein");
        }
        validateWorkPlanFields(request.title(), request.startDate(), request.endDate(), request.approvedHours());
    }

    private void validateWorkPlanFields(String title,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        BigDecimal approvedHours) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titel ist erforderlich");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate und endDate sind erforderlich");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate darf nicht vor startDate liegen");
        }
        if (approvedHours != null && approvedHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("approvedHours darf nicht negativ sein");
        }
    }

    private void validateCreateShiftRequest(WorkPlan workPlan, CreateShiftRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request darf nicht leer sein");
        }
        if (request.employeeId() == null) {
            throw new IllegalArgumentException("employeeId ist erforderlich");
        }
        if (request.shiftDate() == null) {
            throw new IllegalArgumentException("shiftDate ist erforderlich");
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new IllegalArgumentException("startTime und endTime sind erforderlich");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("startTime muss vor endTime liegen");
        }
        if (request.shiftDate().isBefore(workPlan.getStartDate()) || request.shiftDate().isAfter(workPlan.getEndDate())) {
            throw new IllegalArgumentException("Schichtdatum liegt ausserhalb des Arbeitsplans");
        }
    }

    private void validateNoOverlap(CreateShiftRequest request) {
        boolean overlaps = shiftRepository
                .findByEmployeeIdAndShiftDateOrderByStartTimeAsc(request.employeeId(), request.shiftDate())
                .stream()
                .anyMatch(existing -> request.startTime().isBefore(existing.getEndTime())
                        && request.endTime().isAfter(existing.getStartTime()));

        if (overlaps) {
            throw new IllegalStateException("Mitarbeiter ist in diesem Zeitraum bereits eingeplant");
        }
    }

    private void validateExistingShiftsStillFit(WorkPlan workPlan) {
        boolean anyShiftOutsidePeriod = shiftRepository.findByWorkPlanIdOrderByShiftDateAscStartTimeAsc(workPlan.getId())
                .stream()
                .anyMatch(shift -> shift.getShiftDate().isBefore(workPlan.getStartDate())
                        || shift.getShiftDate().isAfter(workPlan.getEndDate()));

        if (anyShiftOutsidePeriod) {
            throw new IllegalStateException("Bestehende Schichten liegen ausserhalb des neuen Planungszeitraums");
        }
    }

    private void ensureDraft(WorkPlan workPlan) {
        if (workPlan.getStatus() == WorkPlanStatus.PUBLISHED) {
            throw new IllegalStateException("Veröffentlichte Arbeitspläne können nicht mehr bearbeitet werden");
        }
    }

    private BigDecimal normalizeHours(BigDecimal hours) {
        return hours == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : hours.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeText(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
