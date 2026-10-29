/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.LoanDao;
import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.dao.ReservationDao;
import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Coordinates reservations and atomically fulfills them as loans. */
public final class ReservationService {
    /** Persistence dependency for reservations and their transactions. */
    private final ReservationDao reservationDao;
    /** Persistence dependency used to validate members. */
    private final MemberDao memberDao;
    /** Persistence dependency for newly created loans. */
    private final LoanDao loanDao;
    /** Catalog service responsible for inventory changes. */
    private final BookService bookService;
    /** Clock used to determine dates. */
    private final Clock clock;

    /** Creates the reservation service.
     * @param reservationDao DAO for reservations
     * @param memberDao DAO for member lookup
     * @param loanDao DAO for loan creation
     * @param bookService catalog service responsible for inventory
     * @param clock clock used to determine the current date
     */
    public ReservationService(ReservationDao reservationDao, MemberDao memberDao,
                              LoanDao loanDao, BookService bookService,
                              Clock clock) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
        this.bookService = bookService;
        this.clock = clock;
    }

    /** Creates a reservation for an existing book and member.
     * @param bookId identifier of the reserved book
     * @param memberId identifier of the reserving member
     * @return the created reservation
     * @throws NotFoundException if the book or member does not exist
     */
    public Reservation reserve(int bookId, int memberId) throws SQLException {
        Book book = bookService.findById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found: " + bookId);
        }
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found: " + memberId);
        }
        Reservation reservation = new Reservation(member, book, LocalDate.now(clock));
        reservationDao.create(reservation);
        return reservation;
    }

    /** Lists all reservations.
     * @return all reservations
     */
    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    /** Fulfills a reservation and creates its loan atomically.
     * @param reservationId identifier of the reservation to fulfill
     * @return the newly created loan
     * @throws SQLException if the transaction fails
     * @throws NotFoundException if the reservation does not exist
     * @throws IllegalStateException if the reservation was already fulfilled
     */
    public Loan fulfill(int reservationId) throws SQLException {
        return reservationDao.transaction(() -> {
            Reservation reservation = reservationDao.findById(reservationId);
            if (reservation == null) {
                throw new NotFoundException("Reservation not found: " + reservationId);
            }
            if (reservation.isFulfilled()) {
                throw new IllegalStateException("Reservation already fulfilled: "
                        + reservationId);
            }
            LocalDate today = LocalDate.now(clock);
            reservation.setFulfilled(true);
            reservationDao.update(reservation);
            bookService.borrow(reservation.getBook().getId());
            Loan loan = new Loan(reservation.getMember(), reservation.getBook(),
                    today, LoanPolicy.dueDate(today));
            loanDao.create(loan);
            return loan;
        });
    }
}
