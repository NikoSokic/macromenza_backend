package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.dto.CestoJedenoDTO;
import com.macromenza.macromenza.dto.JeloDTO;
import com.macromenza.macromenza.dto.JeloDetaljDTO;
import com.macromenza.macromenza.dto.KategorijaJelaDTO;
import com.macromenza.macromenza.services.JeloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jela")
public class JeloController {

    private final JeloService jeloService;
    private final TrenutniKorisnik trenutni;

    public JeloController(JeloService jeloService, TrenutniKorisnik trenutni) {
        this.jeloService = jeloService;
        this.trenutni = trenutni;
    }

    @GetMapping
    public List<JeloDTO> getSvaJela(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kategorija,
            @RequestParam(required = false) BigDecimal minKalorije,
            @RequestParam(required = false) BigDecimal maxKalorije,
            @RequestParam(required = false) BigDecimal minProteini,
            @RequestParam(required = false) BigDecimal maxProteini,
            @RequestParam(required = false) BigDecimal minUgljikohidrati,
            @RequestParam(required = false) BigDecimal maxUgljikohidrati,
            @RequestParam(required = false) BigDecimal minMasti,
            @RequestParam(required = false) BigDecimal maxMasti
    ) {
        return jeloService.dohvatiJela(
                search, kategorija,
                minKalorije, maxKalorije,
                minProteini, maxProteini,
                minUgljikohidrati, maxUgljikohidrati,
                minMasti, maxMasti
        );
    }

    @GetMapping("/kategorije")
    public List<KategorijaJelaDTO> getKategorije() {
        return jeloService.dohvatiKategorije();
    }

    @GetMapping("/cesto")
    public List<CestoJedenoDTO> getCestoJedeno(
            @RequestParam Long idKorisnik,
            @RequestParam(defaultValue = "10") int limit
    ) {
        trenutni.provjeri(idKorisnik);
        return jeloService.dohvatiCestoJedeno(idKorisnik, limit);
    }

    @GetMapping("/{id}")
    public JeloDTO getJeloPoId(@PathVariable Long id) {
        return jeloService.dohvatiJelo(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Jelo ne postoji."));
    }

    @GetMapping("/{id}/detalj")
    public ResponseEntity<JeloDetaljDTO> getJeloDetalj(@PathVariable Long id) {
        return ResponseEntity.of(jeloService.dohvatiDetalj(id));
    }

    @GetMapping("/pretraga")
    public List<JeloDTO> pretraziJela(@RequestParam String naziv) {
        return jeloService.dohvatiJela(naziv);
    }

    @GetMapping("/filter")
    public List<JeloDTO> filtrirajJela(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String kategorija,
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
                search, kategorija,
                minKalorije, maxKalorije,
                minProteini, maxProteini,
                minUgljikohidrati, maxUgljikohidrati,
                minMasti, maxMasti
        );
    }


}
