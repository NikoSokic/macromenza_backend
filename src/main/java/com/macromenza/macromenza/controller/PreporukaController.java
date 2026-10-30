package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.PreporukaRepository;
import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.services.IzracunPreporukeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preporuke")
public class PreporukaController {

    private final PreporukaRepository preporukaRepository;
    private final IzracunPreporukeService izracunPreporukeService;
    private final TrenutniKorisnik trenutni;

    public PreporukaController(
            PreporukaRepository preporukaRepository,
            IzracunPreporukeService izracunPreporukeService,
            TrenutniKorisnik trenutni
    ) {
        this.preporukaRepository = preporukaRepository;
        this.izracunPreporukeService = izracunPreporukeService;
        this.trenutni = trenutni;
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public List<Preporuka> getPreporukeZaKorisnika(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return preporukaRepository.findByIdKorisnik(idKorisnik);
    }

    @PostMapping("/izracunaj/{idKorisnik}")
    public Preporuka izracunajPreporuku(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return izracunPreporukeService.izracunajPreporuku(idKorisnik);
    }

}
