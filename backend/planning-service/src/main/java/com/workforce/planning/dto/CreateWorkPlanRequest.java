package com.workforce.planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO zum Erstellen eines Arbeitsplans.
 *
 * @param title         Titel des Arbeitsplans
 * @param shiftLeadId   ID des verantwortlichen Schichtleiters
 * @param startDate     Startdatum des Planungszeitraums
 * @param endDate       Enddatum des Planungszeitraums
 * @param approvedHours von HR freigegebenes Stundenkontingent
 */
public record CreateWorkPlanRequest(
        String title,
        Long shiftLeadId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal approvedHours
) {}
