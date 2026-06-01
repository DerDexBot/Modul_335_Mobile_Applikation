package com.workforce.planning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA-Entity für die Tabelle {@code shifts}.
 * Speichert eine geplante Schicht eines Mitarbeiters innerhalb eines Arbeitsplans.
 */
@Data
@Entity
@Table(name = "shifts")
public class Shift {

    /** Primärschlüssel, automatisch generiert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Zugehöriger Arbeitsplan. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_plan_id", nullable = false)
    private WorkPlan workPlan;

    /** Optionaler Bezug zu {@code orders.id}; kein JPA-Join wegen eigenem Order-Service. */
    @Column(name = "order_id")
    private Long orderId;

    /** FK zu {@code users.id}; kein JPA-Join wegen eigenem User-Service. */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /** Datum der Schicht. */
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    /** Startzeit der Schicht. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Endzeit der Schicht. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Optionale Notizen zur Schicht. */
    @Column(columnDefinition = "TEXT")
    private String notes;
}
