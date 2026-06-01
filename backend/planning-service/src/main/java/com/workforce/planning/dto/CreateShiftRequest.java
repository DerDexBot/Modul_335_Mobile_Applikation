package com.workforce.planning.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO zum Hinzufügen einer Schicht zu einem Arbeitsplan.
 *
 * @param employeeId ID des Mitarbeiters
 * @param orderId    optionale ID des Auftrags
 * @param shiftDate  Datum der Schicht
 * @param startTime  Startzeit
 * @param endTime    Endzeit
 * @param notes      optionale Notiz
 */
public record CreateShiftRequest(
        Long employeeId,
        Long orderId,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        String notes
) {}
