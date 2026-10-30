package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.dto.DanPregledDTO;
import com.macromenza.macromenza.dto.ObrokDTO;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.dto.TjedanDTO;
import com.macromenza.macromenza.services.ObrokService;
import com.macromenza.macromenza.services.PrimjerTjednaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/obroci")
public class ObrokController {

    private final ObrokService obrokService;

    private final PrimjerTjednaService primjerTjednaService;
    private final TrenutniKorisnik trenutni;

    public ObrokController(ObrokService obrokService, PrimjerTjednaService primjerTjednaService, TrenutniKorisnik trenutni) {
        this.trenutni = trenutni;
        this.obrokService = obrokService;
        this.primjerTjednaService = primjerTjednaService;
    }

    /** Stvara primjer za zadnjih 7 dana (bez danas) bez ijednog obroka; vraća broj stvorenih obroka po danu. */
    @PostMapping("/primjer-tjedna")
    public Map<String, Integer> ucitajPrimjerTjedna(@RequestParam Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return primjerTjednaService.ucitaj(idKorisnik);
    }

    /** Briše samo obroke s izvorom PRIMJER. */
    @DeleteMapping("/primjer-tjedna")
    public Map<String, Integer> ukloniPrimjerTjedna(@RequestParam Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return Map.of("obrisano", primjerTjednaService.ukloni(idKorisnik));
    }

    @PostMapping
    public ObrokDTO spremiObrok(@RequestBody ObrokRequest request) {
        trenutni.provjeri(request.getIdKorisnik());
        return obrokService.spremiObrok(request);
    }

    @GetMapping
    public List<ObrokDTO> getObroci(@RequestParam Long idKorisnik, @RequestParam LocalDate datum) {
        trenutni.provjeri(idKorisnik);
        return obrokService.dohvatiObroke(idKorisnik, datum);
    }

    /** Pregled dana za početni ekran; [datum] je opcionalan, zadano danas (Europe/Zagreb). */
    @GetMapping("/dan-pregled")
    public DanPregledDTO getDanPregled(
            @RequestParam Long idKorisnik,
            @RequestParam(required = false) LocalDate datum
    ) {
        trenutni.provjeri(idKorisnik);
        return obrokService.dohvatiDanPregled(idKorisnik, datum);
    }

    /** Tjedan pon–ned za Povijest; [pocetak] je bilo koji datum tjedna (svodi se na ponedjeljak), zadano tekući tjedan. */
    @GetMapping("/tjedan")
    public TjedanDTO getTjedan(
            @RequestParam Long idKorisnik,
            @RequestParam(required = false) LocalDate pocetak
    ) {
        trenutni.provjeri(idKorisnik);
        return obrokService.dohvatiTjedan(idKorisnik, pocetak);
    }

    @GetMapping("/{id}")
    public ObrokDTO getObrok(@PathVariable Long id, @RequestParam Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return obrokService.dohvatiObrok(id, idKorisnik);
    }

    @PutMapping("/{id}")
    public ObrokDTO azurirajObrok(
            @PathVariable Long id,
            @RequestParam Long idKorisnik,
            @RequestBody ObrokRequest request
    ) {
        trenutni.provjeri(idKorisnik);
        return obrokService.azurirajObrok(id, idKorisnik, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisiObrok(@PathVariable Long id, @RequestParam Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        obrokService.obrisiObrok(id, idKorisnik);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> nijePronaden(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> neispravanZahtjev(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("poruka", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> neispravanFormat(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("poruka", "Neispravan format zahtjeva (datum YYYY-MM-DD, vrijeme HH:mm)."));
    }
}
