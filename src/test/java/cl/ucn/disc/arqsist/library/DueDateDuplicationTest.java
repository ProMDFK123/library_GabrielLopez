/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.ReservationService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies that checkout and reservation fulfillment share the same due-date policy. */
class DueDateDuplicationTest {
    /** Loan service under test. */
    private LoanService loanService;
    /** Reservation service under test. */
    private ReservationService reservationService;
    /** Book borrowed by both tested flows. */
    private Book book;
    /** Member borrowing the book. */
    private Member member;

    /** Creates an isolated database and deterministic services before each test. */
    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());
        BookService bookService = new BookService(bookDao);
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"),
                ZoneOffset.UTC);
        loanService = new LoanService(loanDao, memberDao, bookService, clock);
        reservationService = new ReservationService(reservationDao, memberDao,
                loanDao, bookService, clock);
        book = new Book("Design Patterns", "Gamma et al.",
                "9780201633610", 2);
        bookDao.create(book);
        member = new Member("Grace Hopper", "grace@example.com");
        memberDao.create(member);
    }

    /** Checks that checkout and fulfillment calculate identical due dates. */
    @Test
    void checkoutAndFulfillUseTheSameLoanPeriod() throws Exception {
        Loan fromCheckout = loanService.checkout(member.getId(), book.getId());
        Reservation reservation = reservationService.reserve(book.getId(), member
                .getId());
        Loan fromFulfill = reservationService.fulfill(reservation.getId());
        assertEquals(fromCheckout.getDueDate(), fromFulfill.getDueDate(),
                "the same kind of loan should have the same due date");
    }
}
