/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.controller;

import cl.ucn.disc.arqsist.library.service.LoanService;
import io.javalin.config.JavalinConfig;

/** Registers HTTP routes for loan operations. */
public final class LoanController {
    /** Service that owns lending rules and operations. */
    private final LoanService loanService;

    /** Creates the controller.
     * @param loanService lending service used by the routes
     */
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /** Registers the loan routes.
     * @param config Javalin configuration receiving the routes
     */
    public void register(JavalinConfig config) {
        config.routes.post("/loans", ctx -> {
            int memberId = Integer.parseInt(ctx.queryParam("memberId"));
            int bookId = Integer.parseInt(ctx.queryParam("bookId"));
            ctx.json(loanService.checkout(memberId, bookId));
        });
        config.routes.get("/loans", ctx -> ctx.json(loanService
                .findAll()));
        config.routes.post("/loans/{id}/return", ctx -> ctx
                .json(loanService.returnLoan(Integer.parseInt(ctx
                        .pathParam("id")))));
        config.routes.get("/loans/overdue", ctx -> ctx
                .json(loanService.overdueLoans()));
    }
}
