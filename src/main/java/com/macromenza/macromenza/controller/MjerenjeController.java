package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mjerenja")
public class MjerenjeController {

    private final MjerenjeRepository mjerenjeRepository;

    public MjerenjeController(MjerenjeRepository mjerenjeRepository) {

        this.mjerenjeRepository = mjerenjeRepository;
    }

    @GetMapping
    public List<Mjerenje> getSvaMjerenja() {

        return mjerenjeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mjerenje getMjerenjePoId(@PathVariable Long id) {

        return mjerenjeRepository.findById(id).orElse(null);
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public List<Mjerenje> getMjerenjaZaKorisnika(@PathVariable Long idKorisnik) {
        return mjerenjeRepository.findByIdKorisnik(idKorisnik);
    }

    @PostMapping
    public Mjerenje dodajMjerenje(@RequestBody Mjerenje mjerenje) {

        return mjerenjeRepository.save(mjerenje);
    }

    @DeleteMapping("/{id}")
    public void obrisiMjerenje(@PathVariable Long id) {

        mjerenjeRepository.deleteById(id);
    }
}