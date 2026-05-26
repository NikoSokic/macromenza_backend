package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/konzumacije")
public class KonzumacijaController {

    private final KonzumacijaRepository konzumacijaRepository;

    public KonzumacijaController(KonzumacijaRepository konzumacijaRepository) {
        this.konzumacijaRepository = konzumacijaRepository;
    }

    @GetMapping
    public List<Konzumacija> getSveKonzumacije() {
        return konzumacijaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Konzumacija getKonzumacijaPoId(@PathVariable Long id) {
        return konzumacijaRepository.findById(id).orElse(null);
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public List<Konzumacija> getKonzumacijeZaKorisnika(@PathVariable Long idKorisnik) {
        return konzumacijaRepository.findByIdKorisnik(idKorisnik);
    }

    @PostMapping
    public Konzumacija dodajKonzumaciju(@RequestBody Konzumacija konzumacija) {
        return konzumacijaRepository.save(konzumacija);
    }

    @DeleteMapping("/{id}")
    public void obrisiKonzumaciju(@PathVariable Long id) {
        konzumacijaRepository.deleteById(id);
    }
}