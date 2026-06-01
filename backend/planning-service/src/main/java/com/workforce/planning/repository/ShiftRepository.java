package com.workforce.planning.repository;

import com.workforce.planning.model.Shift;
import com.workforce.planning.model.WorkPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** Spring Data JPA Repository für geplante Schichten. */
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /** Gibt alle Schichten eines Arbeitsplans sortiert zurück. */
    List<Shift> findByWorkPlanIdOrderByShiftDateAscStartTimeAsc(Long workPlanId);

    /** Gibt alle Schichten eines Mitarbeiters an einem bestimmten Tag zurück. */
    List<Shift> findByEmployeeIdAndShiftDateOrderByStartTimeAsc(Long employeeId, LocalDate shiftDate);

    /** Gibt alle veröffentlichten Kalenderschichten eines Mitarbeiters in einem Datumsbereich zurück. */
    @Query("SELECT s FROM Shift s JOIN s.workPlan wp " +
            "WHERE s.employeeId = :employeeId " +
            "AND s.shiftDate BETWEEN :from AND :to " +
            "AND wp.status = :status " +
            "ORDER BY s.shiftDate ASC, s.startTime ASC")
    List<Shift> findCalendarShifts(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") WorkPlanStatus status
    );
}
