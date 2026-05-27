package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profil")
public class ProfilPodaciController {

    private final ProfilPodaciRepository profilRepository;

    public ProfilPodaciController(ProfilPodaciRepository profilRepository) {
        this.profilRepository = profilRepository;
    }

    @GetMapping
    public List<ProfilPodaci> getSviProfili() {
        return profilRepository.findAll();
    }

    @GetMapping("/{id}")
    public ProfilPodaci getProfil(@PathVariable Long id) {
        return profilRepository.findById(id).orElse(null);
    }

    @GetMapping("/korisnik/{idKorisnik}")
    public ProfilPodaci getProfilPoKorisniku(@PathVariable Long idKorisnik) {
        return profilRepository.findByIdKorisnik(idKorisnik).orElse(null);
    }

    @PostMapping
    public ProfilPodaci dodajProfil(@RequestBody ProfilPodaci profil) {
        return profilRepository.save(profil);
    }

    @PutMapping("/{id}")
    public ProfilPodaci azurirajProfil(@PathVariable Long id, @RequestBody ProfilPodaci noviProfil) {

        ProfilPodaci profil =
                profilRepository.findById(id).orElse(null);

        if (profil == null) {
            return null;
        }

        profil.setVisina(noviProfil.getVisina());
        profil.setSpol(noviProfil.getSpol());
        profil.setDob(noviProfil.getDob());

        return profilRepository.save(profil);
    }

    @DeleteMapping("/{id}")
    public void obrisiProfil(@PathVariable Long id) {
        profilRepository.deleteById(id);
    }
}