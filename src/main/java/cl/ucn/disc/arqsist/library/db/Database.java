/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.db;

import cl.ucn.disc.arqsist.library.model.Book;
import cl.ucn.disc.arqsist.library.model.Loan;
import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.model.Reservation;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Owns the database connection and initializes the library schema and sample data. */
public final class Database {
    /** Logger for database setup operations. */
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    /** ORMLite connection source. */
    private final ConnectionSource connectionSource;

    /** Opens the database and creates missing tables. */
    public Database(String jdbcUrl) throws SQLException {
        this.connectionSource = new JdbcConnectionSource(jdbcUrl);
        TableUtils.createTableIfNotExists(connectionSource, Book.class);
        TableUtils.createTableIfNotExists(connectionSource, Member.class);
        TableUtils.createTableIfNotExists(connectionSource, Loan.class);
        TableUtils.createTableIfNotExists(connectionSource, Reservation.class);
    }
    /** @return database connection source */
    public ConnectionSource connectionSource() { return connectionSource; }

    /** Seeds an empty database with sample books, three members, three loans, and one reservation. */
    public void seedIfEmpty() throws SQLException {
        Dao<Book, Integer> bookDao = DaoManager.createDao(connectionSource, Book.class);
        if (bookDao.queryForAll().isEmpty()) {
            log.debug("Seeding books");
            bookDao.create(new Book("Clean Code", "Robert C. Martin",
                    "9780132350884", 3));
            bookDao.create(new Book("The Pragmatic Programmer", "Hunt & Thomas",
                    "9780201616224", 2));
            bookDao.create(new Book("Design Patterns", "Gamma et al.",
                    "9780201633610", 4));
        }
        Dao<Member, Integer> memberDao = DaoManager.createDao(connectionSource, Member.class);
        if (memberDao.queryForAll().isEmpty()) {
            log.debug("Seeding members");
            memberDao.create(new Member("Ada Lovelace", "ada@example.com"));
            memberDao.create(new Member("Grace Hopper", "grace@example.com"));
            memberDao.create(new Member("Alan Turing", "alan@example.com"));
        }
        Dao<Loan, Integer> loanDao = DaoManager.createDao(connectionSource, Loan.class);
        List<Loan> existingLoans = loanDao.queryForAll();
        if (existingLoans.isEmpty()) {
            log.debug("Seeding loans");
            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();
            LocalDate today = LocalDate.now();

            Loan returned = new Loan(members.get(0), books.get(0), today.minusDays(30),
                    today.minusDays(9));
            returned.setReturned(true);
            returned.setReturnDate(today.minusDays(10));
            loanDao.create(returned);

            Loan active = new Loan(members.get(1), books.get(2), today.minusDays(2),
                    today.plusDays(12));
            loanDao.create(active);
            books.get(2).setAvailableCopies(books.get(2).getAvailableCopies() - 1);
            bookDao.update(books.get(2));

            Loan overdue = new Loan(members.get(2), books.get(1), today.minusDays(30),
                    today.minusDays(9));
            loanDao.create(overdue);
            books.get(1).setAvailableCopies(books.get(1).getAvailableCopies() - 1);
            bookDao.update(books.get(1));
        }
        Dao<Reservation, Integer> reservationDao = DaoManager.createDao(connectionSource,
                Reservation.class);
        if (reservationDao.queryForAll().isEmpty()) {
            log.debug("Seeding reservation");
            List<Book> books = bookDao.queryForAll();
            List<Member> members = memberDao.queryForAll();
            reservationDao.create(new Reservation(members.get(0), books.get(1),
                    LocalDate.now().minusDays(1)));
        }
    }
}
