package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;
import com.macromenza.macromenza.security.TrenutniKorisnik;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/profil")
public class ProfilPodaciController {

    private final ProfilPodaciRepository profilRepository;
    private final TrenutniKorisnik trenutni;

    public ProfilPodaciController(ProfilPodaciRepository profilRepository, TrenutniKorisnik trenutni) {
        this.profilRepository = profilRepository;
        this.trenutni = trenutni;
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public ProfilPodaci getProfilPoKorisniku(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return profilRepository.findByIdKorisnik(idKorisnik)
                .orElseThrow(() -> new NoSuchElementException("Profil ne postoji."));
    }

    @PutMapping("/{id}")
    public ProfilPodaci azurirajProfil(@PathVariable Long id, @RequestBody ProfilPodaci noviProfil) {
        ProfilPodaci profil = profilRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Profil ne postoji."));
        trenutni.provjeri(profil.getIdKorisnik());

        profil.setVisina(noviProfil.getVisina());
        profil.setSpol(noviProfil.getSpol());
        profil.setDob(noviProfil.getDob());

        return profilRepository.save(profil);
    }
}
