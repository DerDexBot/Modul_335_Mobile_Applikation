package com.workforce.planning.dto;

import com.workforce.planning.model.Shift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Rückgabe-DTO für eine einzelne geplante Schicht. */
public record ShiftResponse(
        Long id,
        Long workPlanId,
        Long employeeId,
        Long orderId,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        String notes,
        BigDecimal plannedHours
) {
    public static ShiftResponse from(Shift shift, BigDecimal plannedHours) {
        return new ShiftResponse(
                shift.getId(),
                shift.getWorkPlan().getId(),
                shift.getEmployeeId(),
                shift.getOrderId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getNotes(),
                plannedHours
        );
    }
}
