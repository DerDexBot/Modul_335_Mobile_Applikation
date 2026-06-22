package com.workforce.planning.dto;

import java.time.LocalDate;

/**
 * DTO zum Bearbeiten eines Arbeitsplan-Entwurfs.
 * Das Stundenkontingent wird bei Datumsänderungen erneut aus der passenden
 * HR-Stundenfreigabe übernommen und kann nicht direkt geändert werden.
 */
public record UpdateWorkPlanRequest(
        String title,
        LocalDate startDate,
        LocalDate endDate
) {}
