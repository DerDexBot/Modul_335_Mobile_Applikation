package com.workforce.planning.dto;

import java.time.LocalDate;

/**
 * DTO zum Erstellen eines Arbeitsplans.
 *
 * <p>Das Stundenkontingent wird nicht vom Schichtleiter bestimmt.
 * Der Planning Service übernimmt es automatisch aus der HR-Stundenfreigabe
 * für {@code shiftLeadId + Monat/Jahr}.</p>
 */
public record CreateWorkPlanRequest(
        String title,
        Long shiftLeadId,
        LocalDate startDate,
        LocalDate endDate
) {}
