/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Handles reservations and the legacy fulfillment flow until Stage 2. */
public final class ReservationService {
    /** Reservation persistence. */
    private final ReservationDao reservationDao;
    /** Book persistence used by the legacy fulfillment flow. */
    private final BookDao bookDao;
    /** Member persistence. */
    private final MemberDao memberDao;
    /** Loan persistence. */
    private final LoanDao loanDao;

    /** Creates the service with its current Stage 1 dependencies. */
    public ReservationService(ReservationDao reservationDao, BookDao bookDao, MemberDao memberDao, LoanDao loanDao) {
        this.reservationDao = reservationDao; this.bookDao = bookDao; this.memberDao = memberDao; this.loanDao = loanDao;
    }
    /** Creates a reservation. */
    public Reservation reserve(int bookId, int memberId) throws SQLException {
        Book book = bookDao.findById(bookId);
        Member member = memberDao.findById(memberId);
        Reservation reservation = new Reservation(member, book, LocalDate.now());
        reservationDao.create(reservation);
        return reservation;
    }
    /** @return all reservations */
    public List<Reservation> findAll() throws SQLException { return reservationDao.findAll(); }
    /** Fulfills a reservation and creates the corresponding loan. */
    public Loan fulfill(int reservationId) throws SQLException {
        Reservation reservation = reservationDao.findById(reservationId);
        if (reservation == null || reservation.isFulfilled()) throw new IllegalStateException("Reservation not available");
        reservation.setFulfilled(true);
        reservationDao.update(reservation);
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(reservation.getMember(), reservation.getBook(), today, LoanPolicy.dueDate(today));
        loanDao.create(loan);
        return loan;
    }
}
