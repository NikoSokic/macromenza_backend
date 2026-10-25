package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.services.BrisanjePodatakaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/korisnici")
public class KorisnikPodaciController {

    private final BrisanjePodatakaService brisanjePodatakaService;

    public KorisnikPodaciController(BrisanjePodatakaService brisanjePodatakaService) {
        this.brisanjePodatakaService = brisanjePodatakaService;
    }

    /** Briše obroke, mjerenja i preporuke; račun i profil ostaju. Zahtijeva potvrda=OBRISI. */
    @DeleteMapping("/{id}/podaci")
    public Map<String, Long> obrisiPodatke(
            @PathVariable Long id,
            @RequestParam(required = false) String potvrda
    ) {
        return brisanjePodatakaService.obrisiSvePodatke(id, potvrda);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> nijePronaden(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> neispravanZahtjev(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("poruka", e.getMessage()));
    }
}
