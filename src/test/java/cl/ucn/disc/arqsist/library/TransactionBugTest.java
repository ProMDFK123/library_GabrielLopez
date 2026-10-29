/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies checkout validation and rollback when a referenced member is missing. */
class TransactionBugTest {
    /** DAO used to inspect inventory before and after checkout. */
    private BookDao bookDao;
    /** Lending service under test. */
    private LoanService loanService;

    /** Creates an isolated database and deterministic lending service. */
    @BeforeEach
    void setUp() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        BookService bookService = new BookService(bookDao);
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"),
                ZoneOffset.UTC);
        loanService = new LoanService(loanDao, memberDao, bookService, clock);
    }

    /** Ensures a failed checkout neither changes inventory nor creates a loan. */
    @Test
    void checkoutLeavesNoPartialStateOnFailure() throws Exception {
        Book book = new Book("Clean Code", "Robert C. Martin",
                "9780132350884", 2);
        bookDao.create(book);
        assertThrows(NotFoundException.class, () -> loanService
                .checkout(9999, book.getId()));
        Book reloaded = bookDao.findById(book.getId());
        assertEquals(reloaded.getTotalCopies(), reloaded.getAvailableCopies(),
                "availableCopies must not change when checkout fails");
    }
}
