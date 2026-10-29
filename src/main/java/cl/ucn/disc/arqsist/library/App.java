/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.controller.BookController;
import cl.ucn.disc.arqsist.library.controller.LoanController;
import cl.ucn.disc.arqsist.library.controller.MemberController;
import cl.ucn.disc.arqsist.library.controller.ReservationController;
import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.MemberService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import cl.ucn.disc.arqsist.library.service.ReservationService;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Application entry point and dependency composition root. */
public final class App {
    /** Logger for application startup and wiring. */
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /** Prevents instantiation of the application entry point. */
    private App() { }

    /** Creates dependencies, configures HTTP routes, and starts the server.
     * @param args command-line arguments (currently unused)
     * @throws Exception if database initialization fails
     */
    public static void main(String[] args) throws Exception {
        log.info("Opening database");
        Database db = new Database("jdbc:sqlite:database.db");
        log.info("Seeding database when empty");
        db.seedIfEmpty();

        log.info("Creating data access objects");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());

        log.info("Creating services and Santiago clock");
        Clock clock = Clock.system(ZoneId.of("America/Santiago"));
        BookService bookService = new BookService(bookDao);
        MemberService memberService = new MemberService(memberDao);
        LoanService loanService = new LoanService(loanDao, memberDao, bookService, clock);
        ReservationService reservationService = new ReservationService(
                reservationDao, memberDao, loanDao, bookService, clock);

        log.info("Creating controllers");
        BookController bookController = new BookController(bookService);
        MemberController memberController = new MemberController(memberService);
        LoanController loanController = new LoanController(loanService);
        ReservationController reservationController = new ReservationController(reservationService);

        log.info("Configuring HTTP server");
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper ->
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)));
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
            config.routes.exception(NotFoundException.class, (e, ctx) ->
                    ctx.status(404).json(Map.of("error", e.getMessage())));
            config.routes.exception(IllegalArgumentException.class, (e, ctx) ->
                    ctx.status(400).json(Map.of("error", e.getMessage())));
            config.routes.exception(IllegalStateException.class, (e, ctx) ->
                    ctx.status(409).json(Map.of("error", e.getMessage())));
            bookController.register(config);
            memberController.register(config);
            loanController.register(config);
            reservationController.register(config);
        });
        log.info("Starting Library application on port 7070");
        app.start(7070);
    }
}