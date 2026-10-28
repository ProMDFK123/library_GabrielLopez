/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.dao;

import cl.ucn.disc.arqsist.library.model.Book;
import com.j256.ormlite.support.ConnectionSource;

/** Data access object for Book records. */
public final class BookDao extends BaseDao<Book> {
    /** Creates the DAO. */
    public BookDao(ConnectionSource connectionSource) {
        super(connectionSource, Book.class);
    }
}
