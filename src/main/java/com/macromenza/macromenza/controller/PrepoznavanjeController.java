package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.dto.PrijedlogObrokaDTO;
import com.macromenza.macromenza.services.PrepoznavanjeException;
import com.macromenza.macromenza.services.PrepoznavanjeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/prepoznavanje")
public class PrepoznavanjeController {

    private final PrepoznavanjeService prepoznavanjeService;
    private final TrenutniKorisnik trenutni;

    public PrepoznavanjeController(PrepoznavanjeService prepoznavanjeService, TrenutniKorisnik trenutni) {
        this.trenutni = trenutni;
        this.prepoznavanjeService = prepoznavanjeService;
    }

    /** Prijedlog obroka iz slike i/ili opisa; ništa se ne sprema (potvrda ide preko POST /api/obroci). */
    @PostMapping
    public PrijedlogObrokaDTO prepoznaj(
            @RequestParam(required = false) Long idKorisnik,
            @RequestParam(required = false) MultipartFile slika,
            @RequestParam(required = false) String opis
    ) {
        trenutni.provjeri(idKorisnik);
        return prepoznavanjeService.prepoznaj(idKorisnik, slika, opis);
    }

    @ExceptionHandler(PrepoznavanjeException.class)
    public ResponseEntity<Map<String, String>> prepoznavanjeGreska(PrepoznavanjeException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("poruka", e.getMessage()));
    }
}
