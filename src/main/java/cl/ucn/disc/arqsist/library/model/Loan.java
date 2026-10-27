
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.model;

import cl.ucn.disc.arqsist.library.db.LocalDatePersister;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;

/** Represents a book loan and its return state. */
@DatabaseTable(tableName = "loans")
public final class Loan {
    /** Database identifier. */
    @DatabaseField(generatedId = true) private int id;
    /** Member who borrowed the book. */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Member member;
    /** Borrowed book. */
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Book book;
    /** Date on which the loan started. */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate loanDate;
    /** Date on which the loan is due. */
    @DatabaseField(canBeNull = false, persisterClass = LocalDatePersister.class)
    private LocalDate dueDate;
    /** Date on which the book was returned, if any. */
    @DatabaseField(persisterClass = LocalDatePersister.class) private LocalDate returnDate;
    /** Whether the book has been returned. */
    @DatabaseField private boolean returned;
    /** Fee charged when the loan is returned late. */
    @DatabaseField private double overdueFee;

    /** Required by ORMLite. */
    public Loan() {}
    /** Creates a new loan. */
    public Loan(Member member, Book book, LocalDate loanDate, LocalDate dueDate) {
        this.member = member; this.book = book; this.loanDate = loanDate; this.dueDate = dueDate;
        this.returned = false; this.overdueFee = 0.0;
    }
    /** @return database identifier */
    public int getId() { return id; }
    /** @param id database identifier */
    public void setId(int id) { this.id = id; }
    /** @return borrowing member */
    public Member getMember() { return member; }
    /** @param member borrowing member */
    public void setMember(Member member) { this.member = member; }
    /** @return borrowed book */
    public Book getBook() { return book; }
    /** @param book borrowed book */
    public void setBook(Book book) { this.book = book; }
    /** @return loan start date */
    public LocalDate getLoanDate() { return loanDate; }
    /** @param loanDate loan start date */
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    /** @return due date */
    public LocalDate getDueDate() { return dueDate; }
    /** @param dueDate due date */
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    /** @return return date, or null while open */
    public LocalDate getReturnDate() { return returnDate; }
    /** @param returnDate return date */
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    /** @return whether this loan has been returned */
    public boolean isReturned() { return returned; }
    /** @param returned returned state */
    public void setReturned(boolean returned) { this.returned = returned; }
    /** @return accrued or charged overdue fee */
    public double getOverdueFee() { return overdueFee; }
    /** @param overdueFee fee amount */
    public void setOverdueFee(double overdueFee) { this.overdueFee = overdueFee; }
}

