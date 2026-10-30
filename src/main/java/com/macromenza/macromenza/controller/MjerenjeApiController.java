package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.dto.BrzoMjerenjeDTO;
import com.macromenza.macromenza.dto.BrzoMjerenjeRequest;
import com.macromenza.macromenza.dto.MjerenjeMaseDTO;
import com.macromenza.macromenza.services.MjerenjeService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mjerenja")
public class MjerenjeApiController {

    private final MjerenjeService mjerenjeService;
    private final TrenutniKorisnik trenutni;

    public MjerenjeApiController(MjerenjeService mjerenjeService, TrenutniKorisnik trenutni) {
        this.trenutni = trenutni;
        this.mjerenjeService = mjerenjeService;
    }

    /** Mase za graf, po datumu uzlazno (jedna po danu); [od] i [do] su opcionalni. */
    @GetMapping
    public List<MjerenjeMaseDTO> getMase(
            @RequestParam Long idKorisnik,
            @RequestParam(required = false) LocalDate od,
            @RequestParam(name = "do", required = false) LocalDate doDatuma
    ) {
        trenutni.provjeri(idKorisnik);
        return mjerenjeService.dohvatiMase(idKorisnik, od, doDatuma);
    }

    /** Današnja masa u jednom pozivu; vraća spremljeno mjerenje i novu preporuku. */
    @PostMapping("/brzo")
    public BrzoMjerenjeDTO brzoMjerenje(@RequestBody BrzoMjerenjeRequest request) {
        trenutni.provjeri(request.getIdKorisnik());
        return mjerenjeService.brzoMjerenje(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> neispravanZahtjev(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> neispravanFormat(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("poruka", "Neispravan format zahtjeva (idKorisnik broj, masa decimalni broj)."));
    }
}
