package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.PristupZabranjenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Zadani odgovori na greške za sve kontrolere, uvijek kao {"poruka": "..."}.
 * Handleri u pojedinom kontroleru (npr. ObrokController) imaju prednost pred ovima.
 */
@RestControllerAdvice
public class GlobalneGreskeAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalneGreskeAdvice.class);

    @ExceptionHandler(PristupZabranjenException.class)
    public ResponseEntity<Map<String, String>> zabranjeno(PristupZabranjenException e) {
        return odgovor(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    public ResponseEntity<Map<String, String>> nijePronadeno(Exception e) {
        String poruka = e instanceof NoSuchElementException && e.getMessage() != null
                ? e.getMessage()
                : "Traženi resurs ne postoji.";
        return odgovor(HttpStatus.NOT_FOUND, poruka);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> neispravanZahtjev(IllegalArgumentException e) {
        return odgovor(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> necitljivoTijelo(HttpMessageNotReadableException e) {
        return odgovor(HttpStatus.BAD_REQUEST, "Neispravan format zahtjeva.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> nedostajeParametar(MissingServletRequestParameterException e) {
        return odgovor(HttpStatus.BAD_REQUEST, "Nedostaje parametar " + e.getParameterName() + ".");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> krivTipParametra(MethodArgumentTypeMismatchException e) {
        return odgovor(HttpStatus.BAD_REQUEST, "Neispravna vrijednost parametra " + e.getName() + ".");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodaNijeDopustena(HttpRequestMethodNotSupportedException e) {
        return odgovor(HttpStatus.METHOD_NOT_ALLOWED, "Metoda nije dopuštena.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> sukobPodataka(DataIntegrityViolationException e) {
        log.warn("Povreda integriteta podataka", e);
        return odgovor(HttpStatus.CONFLICT, "Podaci se ne mogu spremiti jer su u sukobu s postojećima.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> neocekivano(Exception e) {
        log.error("Neočekivana greška", e);
        return odgovor(HttpStatus.INTERNAL_SERVER_ERROR, "Došlo je do greške na poslužitelju.");
    }

    private static ResponseEntity<Map<String, String>> odgovor(HttpStatus status, String poruka) {
        return ResponseEntity.status(status).body(Map.of("poruka", poruka == null ? status.getReasonPhrase() : poruka));
    }
}
