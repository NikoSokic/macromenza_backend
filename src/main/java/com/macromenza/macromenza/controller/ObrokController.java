package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.ObrokDTO;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.services.ObrokService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/obroci")
public class ObrokController {

    private final ObrokService obrokService;

    public ObrokController(ObrokService obrokService) {
        this.obrokService = obrokService;
    }

    @PostMapping
    public ObrokDTO spremiObrok(@RequestBody ObrokRequest request) {
        return obrokService.spremiObrok(request);
    }

    @GetMapping
    public List<ObrokDTO> getObroci(@RequestParam Long idKorisnik, @RequestParam LocalDate datum) {
        return obrokService.dohvatiObroke(idKorisnik, datum);
    }

    @GetMapping("/{id}")
    public ObrokDTO getObrok(@PathVariable Long id, @RequestParam Long idKorisnik) {
        return obrokService.dohvatiObrok(id, idKorisnik);
    }

    @PutMapping("/{id}")
    public ObrokDTO azurirajObrok(
            @PathVariable Long id,
            @RequestParam Long idKorisnik,
            @RequestBody ObrokRequest request
    ) {
        return obrokService.azurirajObrok(id, idKorisnik, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisiObrok(@PathVariable Long id, @RequestParam Long idKorisnik) {
        obrokService.obrisiObrok(id, idKorisnik);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> nijePronaden(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> neispravanZahtjev(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> neispravanFormat(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("poruka", "Neispravan format zahtjeva (datum YYYY-MM-DD, vrijeme HH:mm)."));
    }
}
