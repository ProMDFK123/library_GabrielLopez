/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/** Provides common CRUD and transaction operations for entity DAOs. */
public abstract class BaseDao<T> {
    /** ORMLite DAO used to persist the entity type. */
    protected final Dao<T, Integer> dao;

    /** Creates a base DAO for the supplied entity class. */
    protected BaseDao(ConnectionSource connectionSource, Class<T> clazz) {
        try { this.dao = DaoManager.createDao(connectionSource, clazz); }
        catch (SQLException e) {
            throw new RuntimeException("Unable to create DAO for " + clazz.getSimpleName(),
                    e); }
    }
    /** @return all persisted entities */
    public List<T> findAll() {
        try { return dao.queryForAll(); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    /** @param id entity identifier @return matching entity, or null */
    public T findById(int id) {
        try { return dao.queryForId(id); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    /** @param entity entity to persist */
    public void create(T entity) {
        try { dao.create(entity); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    /** @param entity entity to update */
    public void update(T entity) {
        try { dao.update(entity); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    /** @param entity entity to delete */
    public void delete(T entity) {
        try { dao.delete(entity); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }
    /** Executes a callable in a database transaction. */
    public <R> R transaction(Callable<R> callable) throws SQLException {
        try {
            return TransactionManager.callInTransaction(dao.getConnectionSource(),
                    callable);
        } catch (SQLException e) {
            if (e.getCause() instanceof RuntimeException cause) throw cause;
            throw e;
        }
    }
}
