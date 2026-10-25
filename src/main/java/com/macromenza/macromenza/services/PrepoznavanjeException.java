package com.macromenza.macromenza.services;

import org.springframework.http.HttpStatus;

/** Greška prepoznavanja s HTTP statusom; poruka nikad ne sadrži ključ ni tijelo odgovora Geminija. */
public class PrepoznavanjeException extends RuntimeException {

    private final HttpStatus status;

    public PrepoznavanjeException(HttpStatus status, String poruka) {
        super(poruka);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
