package com.workforce.planning.repository;

import com.workforce.planning.model.WorkPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA Repository für Arbeitspläne. */
public interface WorkPlanRepository extends JpaRepository<WorkPlan, Long> {

    /** Gibt alle Arbeitspläne eines Schichtleiters sortiert nach Startdatum zurück. */
    List<WorkPlan> findByShiftLeadIdOrderByStartDateDesc(Long shiftLeadId);

    /** Gibt alle Arbeitspläne sortiert nach Startdatum zurück. */
    List<WorkPlan> findAllByOrderByStartDateDesc();
}
