package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.services.KonzumacijaService;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.dto.PovijestDanaDTO;
import com.macromenza.macromenza.dto.TjedniPregledDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/ukupno/{idKorisnik}")
    public UkupniUnosDTO ukupniUnos(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return konzumacijaService.izracunajDnevniUnos(idKorisnik);
    }

    /** Zastarjelo: aplikacija ga ne koristi; Povijest lista tjedne preko GET /api/obroci/tjedan. */
    @Deprecated
    @GetMapping("/povijest/dani/{idKorisnik}")
    public List<LocalDate> povijestDana(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return konzumacijaService.dohvatiPovijestDana(idKorisnik);
    }

    @GetMapping("/povijest/{idKorisnik}")
    public PovijestDanaDTO povijestZaDan(@PathVariable Long idKorisnik, @RequestParam LocalDate datum) {
        trenutni.provjeri(idKorisnik);
        return konzumacijaService.dohvatiPovijestZaDan(idKorisnik, datum);
    }

    /** Zastarjelo: koristi GET /api/obroci/tjedan (tjedan pon–ned, cilj po danu, jedan upit). */
    @Deprecated
    @GetMapping("/tjedni-pregled/{idKorisnik}")
    public TjedniPregledDTO tjedniPregled(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return konzumacijaService.dohvatiTjedniPregled(idKorisnik);
    }

}