package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.services.KonzumacijaService;
import com.macromenza.macromenza.dto.PovijestDanaDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/konzumacije")
public class KonzumacijaController {

    private final KonzumacijaService konzumacijaService;
    private final TrenutniKorisnik trenutni;

    public KonzumacijaController(
            KonzumacijaService konzumacijaService,
            TrenutniKorisnik trenutni
    ) {
        this.konzumacijaService = konzumacijaService;
        this.trenutni = trenutni;
    }

    @GetMapping("/povijest/{idKorisnik}")
    public PovijestDanaDTO povijestZaDan(@PathVariable Long idKorisnik, @RequestParam LocalDate datum) {
        trenutni.provjeri(idKorisnik);
        return konzumacijaService.dohvatiPovijestZaDan(idKorisnik, datum);
    }

}