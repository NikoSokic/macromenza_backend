package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Korisnik;
import com.macromenza.macromenza.repository.KorisnikRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/korisnici")
public class KorisnikController {

    private final KorisnikRepository korisnikRepository;

    public KorisnikController(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    @GetMapping
    public List<Korisnik> getSviKorisnici() {
        return korisnikRepository.findAll();
    }

    @GetMapping("/{id}")
    public Korisnik getKorisnikPoId(@PathVariable Long id) {
        return korisnikRepository.findById(id).orElse(null);
    }

    @GetMapping("/supabase/{supabaseUid}")
    public Korisnik getKorisnikPoSupabaseUid(@PathVariable UUID supabaseUid) {
        return korisnikRepository.findBySupabaseUid(supabaseUid).orElse(null);
    }

    @PostMapping
    public Korisnik dodajKorisnika(@RequestBody Korisnik korisnik) {
        return korisnikRepository.save(korisnik);
    }

    @PostMapping("/supabase")
    public Korisnik pronadiIliDodajKorisnikaPrekoSupabase(@RequestBody Korisnik noviKorisnik) {
        if (noviKorisnik.getSupabaseUid() == null) {
            return null;
        }

        return korisnikRepository
                .findBySupabaseUid(noviKorisnik.getSupabaseUid())
                .orElseGet(() -> korisnikRepository.save(noviKorisnik));
    }

    @PutMapping("/{id}")
    public Korisnik azurirajKorisnika(@PathVariable Long id, @RequestBody Korisnik noviKorisnik) {
        Korisnik korisnik = korisnikRepository.findById(id).orElse(null);

        if (korisnik == null) {
            return null;
        }

        korisnik.setIme(noviKorisnik.getIme());
        korisnik.setPrezime(noviKorisnik.getPrezime());
        korisnik.setEmail(noviKorisnik.getEmail());
        korisnik.setLozinka_hash(noviKorisnik.getLozinka_hash());

        return korisnikRepository.save(korisnik);
    }

    @DeleteMapping("/{id}")
    public void obrisiKorisnika(@PathVariable Long id) {
        korisnikRepository.deleteById(id);
    }
}