/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Handles loan listing and returns during Stage 1. */
public final class LoanService {
    /** Loan persistence. */
    private final LoanDao loanDao;
    /** Book persistence used by the current return flow. */
    private final BookDao bookDao;

    /** Creates the service with its current Stage 1 dependencies. */
    public LoanService(LoanDao loanDao, BookDao bookDao) {
        this.loanDao = loanDao; this.bookDao = bookDao;
    }
    /** @return all loans */
    public List<Loan> findAll() throws SQLException { return loanDao.findAll(); }
    /** Returns a loan and updates its overdue fee and book inventory. */
    public Loan returnLoan(int loanId) throws SQLException {
        Loan loan = loanDao.findById(loanId);
        if (loan == null || loan.isReturned()) return loan;
        loan.setReturned(true);
        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);
        LocalDate due = loan.getDueDate();
        if (today.isAfter(due)) {
            long daysOverdue = ChronoUnit.DAYS.between(due, today);
            loan.setOverdueFee(daysOverdue * LoanPolicy.FEE_PER_DAY);
        }
        loanDao.update(loan);
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookDao.update(book);
        return loan;
    }
    /** @return all open loans whose due date has passed */
    public List<Loan> overdueLoans() throws SQLException {
        LocalDate today = LocalDate.now();
        return loanDao.findAll().stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate().isBefore(today))
                .toList();
    }
}
