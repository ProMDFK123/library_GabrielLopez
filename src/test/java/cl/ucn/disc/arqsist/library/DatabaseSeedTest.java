/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies the seed data shape, inventory consistency, and idempotence. */
class DatabaseSeedTest {
    /** Ensures seeded loans and reservations exist and repeated seeding is safe. */
    @Test
    void seedCreatesConsistentDataAndCanRunTwice() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        db.seedIfEmpty();
        BookDao bookDao = new BookDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        ReservationDao reservationDao = new ReservationDao(db.connectionSource());
        List<Loan> loans = loanDao.findAll();
        List<Reservation> reservations = reservationDao.findAll();
        List<Book> books = bookDao.findAll();
        assertFalse(loans.isEmpty(), "seed must create loans");
        assertFalse(reservations.isEmpty(), "seed must create reservations");
        LocalDate today = LocalDate.now();
        assertTrue(loans.stream().anyMatch(loan -> !loan.isReturned() &&
                        loan.getDueDate().isBefore(today)),
                "seed must include an overdue open loan");
        for (Book book : books) {
            boolean hasOpenLoan = loans.stream().anyMatch(loan ->
                    !loan.isReturned() && loan.getBook().getId() == book.getId());
            if (hasOpenLoan) {
                assertTrue(book.getAvailableCopies() < book.getTotalCopies(),
                        "a book with an open loan must have fewer available copies");
            }
        }
        int loanCount = loans.size();
        int reservationCount = reservations.size();
        db.seedIfEmpty();
        assertEquals(loanCount, loanDao.findAll().size(),
                "running the seed twice must not add loans");
        assertEquals(reservationCount, reservationDao.findAll().size(),
                "running the seed twice must not add reservations");
    }
}
//aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa