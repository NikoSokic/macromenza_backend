package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.JeloDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.services.JeloService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jela")
public class JeloController {

    private final JeloRepository jeloRepository;
    private final JeloService jeloService;

    public JeloController(JeloRepository jeloRepository, JeloService jeloService) {
        this.jeloRepository = jeloRepository;
        this.jeloService = jeloService;
    }

    @GetMapping
    public List<JeloDTO> getSvaJela(
            @RequestParam(required = false) String search
    ) {
        return jeloService.dohvatiJela(search);
    }

    @GetMapping("/{id}")
    public JeloDTO getJeloPoId(@PathVariable Long id) {
        return jeloService.dohvatiJelo(id).orElse(null);
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
    public List<JeloDTO> pretraziJela(@RequestParam String naziv) {
        return jeloService.dohvatiJela(naziv);
    }

    @GetMapping("/filter")
    public List<JeloDTO> filtrirajJela(
            @RequestParam(required = false) BigDecimal minKalorije,
            @RequestParam(required = false) BigDecimal maxKalorije,
            @RequestParam(required = false) BigDecimal minProteini,
            @RequestParam(required = false) BigDecimal maxProteini,
            @RequestParam(required = false) BigDecimal minUgljikohidrati,
            @RequestParam(required = false) BigDecimal maxUgljikohidrati,
            @RequestParam(required = false) BigDecimal minMasti,
            @RequestParam(required = false) BigDecimal maxMasti
    ) {
        return jeloService.filtrirajJela(
                minKalorije, maxKalorije,
                minProteini, maxProteini,
                minUgljikohidrati, maxUgljikohidrati,
                minMasti, maxMasti
        );
    }


}
