package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.services.BrisanjePodatakaService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/korisnici")
public class KorisnikPodaciController {

    private final BrisanjePodatakaService brisanjePodatakaService;
    private final TrenutniKorisnik trenutni;

    public KorisnikPodaciController(BrisanjePodatakaService brisanjePodatakaService, TrenutniKorisnik trenutni) {
        this.brisanjePodatakaService = brisanjePodatakaService;
        this.trenutni = trenutni;
    }

    /** Briše obroke, mjerenja i preporuke; račun i profil ostaju. Zahtijeva potvrda=OBRISI. */
    @DeleteMapping("/{id}/podaci")
    public Map<String, Long> obrisiPodatke(
            @PathVariable Long id,
            @RequestParam(required = false) String potvrda
    ) {
        trenutni.provjeri(id);
        return brisanjePodatakaService.obrisiSvePodatke(id, potvrda);
    }
}
