package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preporuke")
public class PreporukaController {

    private final PreporukaRepository preporukaRepository;

    public PreporukaController(PreporukaRepository preporukaRepository) {
        this.preporukaRepository = preporukaRepository;
    }

    @GetMapping
    public List<Preporuka> getSvePreporuke() {
        return preporukaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Preporuka getPreporukaPoId(@PathVariable Long id) {
        return preporukaRepository.findById(id).orElse(null);
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public List<Preporuka> getPreporukeZaKorisnika(@PathVariable Long idKorisnik) {
        return preporukaRepository.findByIdKorisnik(idKorisnik);
    }

    @PostMapping
    public Preporuka dodajPreporuku(@RequestBody Preporuka preporuka) {
        return preporukaRepository.save(preporuka);
    }

    @DeleteMapping("/{id}")
    public void obrisiPreporuku(@PathVariable Long id) {
        preporukaRepository.deleteById(id);
    }
}