package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import com.macromenza.macromenza.security.TrenutniKorisnik;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mjerenja")
public class MjerenjeController {

    private final MjerenjeRepository mjerenjeRepository;
    private final TrenutniKorisnik trenutni;

    public MjerenjeController(MjerenjeRepository mjerenjeRepository, TrenutniKorisnik trenutni) {
        this.mjerenjeRepository = mjerenjeRepository;
        this.trenutni = trenutni;
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public List<Mjerenje> getMjerenjaZaKorisnika(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return mjerenjeRepository.findByIdKorisnik(idKorisnik);
    }

    @PostMapping
    public Mjerenje dodajMjerenje(@RequestBody Mjerenje mjerenje) {
        trenutni.provjeri(mjerenje.getIdKorisnik());
        return mjerenjeRepository.save(mjerenje);
    }
}
