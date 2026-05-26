package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.services.KonzumacijaService;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.dto.PovijestDanaDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/konzumacije")
public class KonzumacijaController {

    private final KonzumacijaRepository konzumacijaRepository;
    private final KonzumacijaService konzumacijaService;

    public KonzumacijaController(
            KonzumacijaRepository konzumacijaRepository,
            KonzumacijaService konzumacijaService
    ) {
        this.konzumacijaRepository = konzumacijaRepository;
        this.konzumacijaService = konzumacijaService;
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
        return konzumacijaService.dodajKonzumaciju(konzumacija);
    }

    @DeleteMapping("/{id}")
    public void obrisiKonzumaciju(@PathVariable Long id) {
        konzumacijaRepository.deleteById(id);
    }

    @GetMapping("/ukupno/{idKorisnik}")
    public UkupniUnosDTO ukupniUnos(
            @PathVariable Long idKorisnik
    ) {
        return konzumacijaService
                .izracunajDnevniUnos(idKorisnik);
    }
    @GetMapping("/povijest/dani/{idKorisnik}")
    public List<LocalDate> povijestDana(
            @PathVariable Long idKorisnik
    ){

        return konzumacijaService
                .dohvatiPovijestDana(
                        idKorisnik
                );
    }
    @GetMapping("/povijest/{idKorisnik}")
    public PovijestDanaDTO povijestZaDan(

            @PathVariable Long idKorisnik,

            @RequestParam LocalDate datum

    ){

        return konzumacijaService
                .dohvatiPovijestZaDan(
                        idKorisnik,
                        datum
                );
    }
}