/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

/** Indicates that a requested domain entity does not exist. */
public final class NotFoundException extends RuntimeException {
    /** Creates a not-found exception. */
    public NotFoundException(String message) { super(message); }
}
