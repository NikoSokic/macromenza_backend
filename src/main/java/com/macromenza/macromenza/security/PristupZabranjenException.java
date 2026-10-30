package com.macromenza.macromenza.security;

/** Prijavljeni korisnik traži tuđe podatke (HTTP 403). */
public class PristupZabranjenException extends RuntimeException {

    public PristupZabranjenException(String poruka) {
        super(poruka);
    }
}
