/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.db.Database;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.BookService;
import cl.ucn.disc.arqsist.library.service.LoanService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies overdue fees accrued by open loans when loan lists are requested. */
class AccruedFeeTest {
    /** Ensures overdue loans accrue the expected fee and active loans do not. */
    @Test
    void findAllCalculatesAccruedFeesWithoutChargingActiveLoans() throws Exception {
        Database db = new Database("jdbc:sqlite::memory:");
        BookDao bookDao = new BookDao(db.connectionSource());
        MemberDao memberDao = new MemberDao(db.connectionSource());
        LoanDao loanDao = new LoanDao(db.connectionSource());
        Book book = new Book("Clean Code", "Robert C. Martin",
                "9780132350884", 2);
        bookDao.create(book);
        Member member = new Member("Ada Lovelace", "ada@example.com");
        memberDao.create(member);
        Clock clock = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"),
                ZoneOffset.UTC);
        LoanService loanService = new LoanService(loanDao, memberDao,
                new BookService(bookDao), clock);
        Loan overdue = new Loan(member, book, LocalDate.parse("2025-12-01"),
                LocalDate.parse("2025-12-22"));
        Loan active = new Loan(member, book, LocalDate.parse("2026-01-10"),
                LocalDate.parse("2026-01-25"));
        loanDao.create(overdue);
        loanDao.create(active);

        List<Loan> loans = loanService.findAll();
        Loan loadedOverdue = loans.stream().filter(loan ->
                loan.getId() == overdue.getId()).findFirst().orElseThrow();
        Loan loadedActive = loans.stream().filter(loan ->
                loan.getId() == active.getId()).findFirst().orElseThrow();
        assertEquals(24.0, loadedOverdue.getOverdueFee(), 0.001);
        assertEquals(0.0, loadedActive.getOverdueFee(), 0.001);
    }
}
