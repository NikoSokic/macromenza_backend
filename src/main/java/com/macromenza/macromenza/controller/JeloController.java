package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.repository.JeloRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jela")
public class JeloController {

    private final JeloRepository jeloRepository;

    public JeloController(JeloRepository jeloRepository) {
        this.jeloRepository = jeloRepository;
    }

    @GetMapping
    public List<Jelo> getSvaJela() {
        return jeloRepository.findAll();
    }

    @GetMapping("/{id}")
    public Jelo getJeloPoId(@PathVariable Long id) {
        return jeloRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Jelo dodajJelo(@RequestBody Jelo jelo) {
        return jeloRepository.save(jelo);
    }

    @PutMapping("/{id}")
    public Jelo azurirajJelo(@PathVariable Long id, @RequestBody Jelo novoJelo) {
        Jelo jelo = jeloRepository.findById(id).orElse(null);

        if (jelo == null) {
            return null;
        }

        jelo.setNaziv(novoJelo.getNaziv());
        jelo.setOpis(novoJelo.getOpis());
        jelo.setKalorije(novoJelo.getKalorije());
        jelo.setProteini(novoJelo.getProteini());
        jelo.setUgljikohidrati(novoJelo.getUgljikohidrati());
        jelo.setMasti(novoJelo.getMasti());

        return jeloRepository.save(jelo);
    }

    @DeleteMapping("/{id}")
    public void obrisiJelo(@PathVariable Long id) {
        jeloRepository.deleteById(id);
    }

    @GetMapping("/pretraga")
    public List<Jelo> pretraziJela(
            @RequestParam String naziv
    ) {
        return jeloRepository.findByNazivContainingIgnoreCase(naziv);
    }

}