/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.BookDao;
import cl.ucn.disc.arqsist.library.model.Book;
import java.sql.SQLException;
import java.util.List;

/** Implements catalog operations and protects inventory invariants. */
public final class BookService {
    /** Book persistence. */
    private final BookDao dao;
    /** Creates the service. */
    public BookService(BookDao dao) { this.dao = dao; }
    /** @return all books */
    public List<Book> listAll() throws SQLException { return dao.findAll(); }
    /** Finds a book by identifier. */
    public Book findById(int id) throws SQLException { return dao.findById(id); }
    /** Creates a book and initializes its available stock. */
    public Book create(Book book) throws SQLException {
        book.setAvailableCopies(book.getTotalCopies()); dao.create(book); return book;
    }
    /** Borrows one available copy. */
    public void borrow(int bookId) throws SQLException {
        Book book = dao.findById(bookId);
        if (book == null) throw new NotFoundException("Book not found: " + bookId);
        if (book.getAvailableCopies() <= 0) throw new IllegalStateException("No available copies of book " + bookId);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        dao.update(book);
    }
    /** Registers the return of one copy. */
    public void returnCopy(int bookId) throws SQLException {
        Book book = dao.findById(bookId);
        if (book == null) throw new NotFoundException("Book not found: " + bookId);
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        dao.update(book);
    }
}
