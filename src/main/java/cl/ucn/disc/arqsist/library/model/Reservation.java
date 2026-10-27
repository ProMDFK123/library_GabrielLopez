/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.model;

import cl.ucn.disc.arqsist.library.db.LocalDatePersister;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;

/** Represents a member's reservation of a book. */
@DatabaseTable(tableName = "reservations")
public final class Reservation {
    /** Database identifier. */
    @DatabaseField(generatedId = true) private int id;
    /** Member who made the reservation. */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Member member;
    /** Reserved book. */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Book book;
    /** Date on which the reservation was made. */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate reservedAt;
    /** Whether the reservation has been fulfilled. */
    @DatabaseField private boolean fulfilled;

    /** Required by ORMLite. */
    public Reservation() {}
    /** Creates a reservation. */
    public Reservation(Member member, Book book, LocalDate reservedAt) {
        this.member = member; this.book = book; this.reservedAt = reservedAt;
        this.fulfilled = false;
    }
    /** @return database identifier */
    public int getId() { return id; }
    /** @param id database identifier */
    public void setId(int id) { this.id = id; }
    /** @return reserving member */
    public Member getMember() { return member; }
    /** @param member reserving member */
    public void setMember(Member member) { this.member = member; }
    /** @return reserved book */
    public Book getBook() { return book; }
    /** @param book reserved book */
    public void setBook(Book book) { this.book = book; }
    /** @return reservation date */
    public LocalDate getReservedAt() { return reservedAt; }
    /** @param reservedAt reservation date */
    public void setReservedAt(LocalDate reservedAt) { this.reservedAt = reservedAt; }
    /** @return whether the reservation has been fulfilled */
    public boolean isFulfilled() { return fulfilled; }
    /** @param fulfilled fulfilled state */
    public void setFulfilled(boolean fulfilled) { this.fulfilled = fulfilled; }
}