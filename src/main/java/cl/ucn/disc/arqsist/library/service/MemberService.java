/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Handles member operations and the legacy checkout flow until Stage 2. */
public final class MemberService {
    /** Member persistence. */
    private final MemberDao memberDao;
    /** Book persistence used by the legacy checkout flow. */
    private final BookDao bookDao;
    /** Loan persistence used by the legacy checkout flow. */
    private final LoanDao loanDao;

    /** Creates the service with its current Stage 1 dependencies. */
    public MemberService(MemberDao memberDao, BookDao bookDao, LoanDao loanDao) {
        this.memberDao = memberDao; this.bookDao = bookDao; this.loanDao = loanDao;
    }
    /** Registers a member. */
    public Member register(Member member) throws SQLException {
        memberDao.create(member); return member;
    }
    /** @return all members */
    public List<Member> findAll() throws SQLException { return memberDao.findAll(); }
    /** Checks out a book using the centralized loan policy. */
    public Loan checkout(int memberId, int bookId) throws SQLException {
        Member member = memberDao.findById(memberId);
        Book book = bookDao.findById(bookId);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookDao.update(book);
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(member, book, today, LoanPolicy.dueDate(today));
        loanDao.create(loan);
        return loan;
    }
}
