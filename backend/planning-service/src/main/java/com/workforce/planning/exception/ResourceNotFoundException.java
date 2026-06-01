package com.workforce.planning.exception;

/** Exception für nicht gefundene Planning-Ressourcen. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
