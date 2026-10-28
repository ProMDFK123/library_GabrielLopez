/*
 * Copyright (c) 2026.
 * All rights reserved.
 */
package cl.ucn.disc.arqsist.library.db;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Persister for converting {@link LocalDate} values to and from SQL dates.
 *
 * @author
 */
public class LocalDatePersister extends BaseDataType {

    private static final LocalDatePersister SINGLETON =
            new LocalDatePersister();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Creates the persister for SQL date values.
     */
    private LocalDatePersister() {
        super(SqlType.STRING, new Class<?>[]{LocalDate.class});
    }

    /**
     * Returns the singleton instance of this persister.
     *
     * @return the persister instance
     */
    public static LocalDatePersister getSingleton() {
        return SINGLETON;
    }

    /**
     * Converts a database value into a Java LocalDate.
     *
     * @param fieldType field metadata
     * @param results database results
     * @param columnPos column position
     * @return the parsed LocalDate, or null
     * @throws SQLException if the database value cannot be read
     */
    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results,
            int columnPos
    ) throws SQLException {
        String value = results.getString(columnPos);

        if (value == null) return null;

        return LocalDate.parse(value, FORMATTER);
    }

    /**
     * Converts a SQL argument into a LocalDate.
     *
     /**
     * Converts a SQL argument into a LocalDate.
     *
     * @param fieldType field metadata
     * @param sqlArg SQL argument
     * @param columnPos column position
     * @return the parsed LocalDate, or null
     * @throws SQLException if conversion fails
     */
    @Override
    public Object sqlArgToJava(
            FieldType fieldType,
            Object sqlArg,
            int columnPos
    ) throws SQLException {
        if (sqlArg == null) return null;

        if (sqlArg instanceof LocalDate) return sqlArg;

        return LocalDate.parse(sqlArg.toString(), FORMATTER);
    }

    /**
     * Converts a Java LocalDate into a SQL-compatible argument.
     *
     * @param fieldType field metadata
     * @param obj Java value
     * @return the formatted date, or null
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object obj) {
        if (obj == null) return null;

        return ((LocalDate) obj).format(FORMATTER);
    }

    /**
     * Parses a default value declared in the field definition.
     *
     * @param fieldType field metadata
     * @param defaultStr default value
     * @return the parsed LocalDate, or null
     */
    @Override
    public Object parseDefaultString(
            FieldType fieldType,
            String defaultStr
    ) {
        if (defaultStr == null) return null;

        return LocalDate.parse(defaultStr, FORMATTER);
    }
}