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
    public List<Jelo> getSvaJela(
            @RequestParam(required = false) String search
    ) {
        if (search != null && !search.isBlank()) {
            return jeloRepository.findByNazivContainingIgnoreCase(search);
        }

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
    public List<Jelo> pretraziJela(@RequestParam String naziv) {
        return jeloRepository.findByNazivContainingIgnoreCase(naziv);
    }

    @GetMapping("/filter")
    public List<Jelo> filtrirajJela(
            @RequestParam(required = false) Integer minKalorije,
            @RequestParam(required = false) Integer maxKalorije,
            @RequestParam(required = false) Integer minProteini,
            @RequestParam(required = false) Integer maxProteini,
            @RequestParam(required = false) Integer minUgljikohidrati,
            @RequestParam(required = false) Integer maxUgljikohidrati,
            @RequestParam(required = false) Integer minMasti,
            @RequestParam(required = false) Integer maxMasti
    ) {
        return jeloRepository.findAll()
                .stream()
                .filter(jelo -> minKalorije == null || jelo.getKalorije() >= minKalorije)
                .filter(jelo -> maxKalorije == null || jelo.getKalorije() <= maxKalorije)
                .filter(jelo -> minProteini == null || jelo.getProteini() >= minProteini)
                .filter(jelo -> maxProteini == null || jelo.getProteini() <= maxProteini)
                .filter(jelo -> minUgljikohidrati == null || jelo.getUgljikohidrati() >= minUgljikohidrati)
                .filter(jelo -> maxUgljikohidrati == null || jelo.getUgljikohidrati() <= maxUgljikohidrati)
                .filter(jelo -> minMasti == null || jelo.getMasti() >= minMasti)
                .filter(jelo -> maxMasti == null || jelo.getMasti() <= maxMasti)
                .toList();
    }


}