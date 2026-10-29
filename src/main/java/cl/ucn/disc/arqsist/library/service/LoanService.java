/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Coordinates loan checkout, returns, and accrued overdue fees. */
public final class LoanService {
    /** Persistence dependency for loans. */
    private final LoanDao loanDao;
    /** Persistence dependency used to validate borrowers. */
    private final MemberDao memberDao;
    /** Catalog service that owns inventory changes. */
    private final BookService bookService;
    /** Clock used to make date-dependent behavior deterministic. */
    private final Clock clock;

    /** Creates the loan service.
     * @param loanDao DAO used to persist loans and execute transactions
     * @param memberDao DAO used to validate members
     * @param bookService catalog service responsible for book inventory
     * @param clock clock used to determine the current date
     */
    public LoanService(LoanDao loanDao, MemberDao memberDao,
                       BookService bookService, Clock clock) {
        this.loanDao = loanDao;
        this.memberDao = memberDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /** Lists all loans, calculating accrued fees for open overdue loans in memory.
     * @return loans with current accrued fees
     */
    public List<Loan> findAll() {
        return withAccruedFees(loanDao.findAll());
    }

    /** Checks out a book to a member atomically.
     * @param memberId identifier of the borrowing member
     * @param bookId identifier of the book to borrow
     * @return the created loan
     * @throws SQLException if the database transaction fails
     * @throws NotFoundException if the member or book does not exist
     */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }
        Book book = bookService.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        LocalDate today = LocalDate.now(clock);
        Loan loan = new Loan(member, book, today, LoanPolicy.dueDate(today));
        return loanDao.transaction(() -> {
            bookService.borrow(bookId);
            loanDao.create(loan);
            return loan;
        });
    }

    /** Returns a loan and restores its book inventory in one transaction.
     * @param loanId identifier of the loan to return
     * @return the returned loan
     * @throws SQLException if the database transaction fails
     * @throws NotFoundException if the loan does not exist
     * @throws IllegalStateException if the loan has already been returned
     */
    public Loan returnLoan(int loanId) throws SQLException {
        return loanDao.transaction(() -> {
            Loan loan = loanDao.findById(loanId);
            if (loan == null) {
                throw new NotFoundException("Loan not found: " + loanId);
            }
            if (loan.isReturned()) {
                throw new IllegalStateException("Loan already returned: " + loanId);
            }
            LocalDate today = LocalDate.now(clock);
            loan.setReturned(true);
            loan.setReturnDate(today);
            if (today.isAfter(loan.getDueDate())) {
                loan.setOverdueFee(accruedFee(loan, today));
            }
            loanDao.update(loan);
            bookService.returnCopy(loan.getBook().getId());
            return loan;
        });
    }

    /** Lists open loans whose due date is before today.
     * @return open overdue loans
     */
    public List<Loan> overdueLoans() {
        LocalDate today = LocalDate.now(clock);
        return loanDao.findAll().stream()
                .filter(loan -> !loan.isReturned() && loan.getDueDate()
                        .isBefore(today))
                .toList();
    }

    /** Computes and assigns accrued fees without persisting the modified loans.
     * @param loans loans to inspect
     * @return the same loan list with accrued fees updated in memory
     */
    private List<Loan> withAccruedFees(List<Loan> loans) {
        LocalDate today = LocalDate.now(clock);
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(today)) {
                loan.setOverdueFee(accruedFee(loan, today));
            } else if (!loan.isReturned()) {
                loan.setOverdueFee(0.0);
            }
        }
        return loans;
    }

    /** Calculates the fee accrued since a loan's due date.
     * @param loan loan whose overdue period is calculated
     * @param today date used as the current day
     * @return fee amount at the policy rate
     */
    private double accruedFee(Loan loan, LocalDate today) {
        return ChronoUnit.DAYS.between(loan.getDueDate(), today) * LoanPolicy
                .FEE_PER_DAY;
    }
}