/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import java.time.LocalDate;

/** Centralized business policy for loan periods and overdue fees. */
public final class LoanPolicy {
    /** Standard number of days in a loan period. */
    public static final int DUE_DAYS = 21;
    /** Fee charged per overdue day. */
    public static final double FEE_PER_DAY = 1.0;
    /** Prevents instantiation. */
    private LoanPolicy() {}
    /** Calculates a loan due date. */
    public static LocalDate dueDate(LocalDate loanDate) {
        return loanDate.plusDays(DUE_DAYS);
    }
}
