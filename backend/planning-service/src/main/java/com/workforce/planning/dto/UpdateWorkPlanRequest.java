package com.workforce.planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO zum Bearbeiten eines Arbeitsplan-Entwurfs. */
public record UpdateWorkPlanRequest(
        String title,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal approvedHours
) {}
