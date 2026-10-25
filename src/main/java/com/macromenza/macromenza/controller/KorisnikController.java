package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Korisnik;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import org.springframework.web.bind.annotation.*;
import com.macromenza.macromenza.dto.RegistracijaProfilRequest;
import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;
import com.macromenza.macromenza.services.IzracunPreporukeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/korisnici")
public class KorisnikController {

    private final KorisnikRepository korisnikRepository;
    private final MjerenjeRepository mjerenjeRepository;
    private final ProfilPodaciRepository profilPodaciRepository;
    private final IzracunPreporukeService izracunPreporukeService;

    public KorisnikController(
            KorisnikRepository korisnikRepository,
            MjerenjeRepository mjerenjeRepository,
            ProfilPodaciRepository profilPodaciRepository,
            IzracunPreporukeService izracunPreporukeService
    ) {
        this.korisnikRepository = korisnikRepository;
        this.mjerenjeRepository = mjerenjeRepository;
        this.profilPodaciRepository = profilPodaciRepository;
        this.izracunPreporukeService = izracunPreporukeService;
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
    public Korisnik pronadiIliDodajKorisnikaPrekoSupabase(
            @RequestBody Korisnik noviKorisnik
    ) {
        if (noviKorisnik.getSupabaseUid() == null) {
            return null;
        }

        return korisnikRepository
                .findBySupabaseUid(noviKorisnik.getSupabaseUid())
                .orElseGet(() -> korisnikRepository.save(noviKorisnik));
    }

    @PutMapping("/{id}")
    public Korisnik azurirajKorisnika(
            @PathVariable Long id,
            @RequestBody Korisnik noviKorisnik
    ) {
        Korisnik korisnik =
                korisnikRepository.findById(id).orElse(null);

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

    @GetMapping("/{id}/onboarding-status")
    public Map<String, Boolean> getOnboardingStatus(@PathVariable Long id) {

        Korisnik korisnik =
                korisnikRepository.findById(id).orElse(null);

        if (korisnik == null) {
            return Map.of(
                    "onboardingZavrsen",
                    false
            );
        }

        boolean imaOsnovnePodatke =
                nijePrazno(korisnik.getIme()) &&
                        nijePrazno(korisnik.getPrezime());

        boolean imaMjerenje =
                mjerenjeRepository
                        .findTopByIdKorisnikOrderByIdDesc(id)
                        .isPresent();

        boolean onboardingZavrsen =
                imaOsnovnePodatke && imaMjerenje;

        return Map.of(
                "onboardingZavrsen",
                onboardingZavrsen
        );
    }

    private boolean nijePrazno(String vrijednost) {
        return vrijednost != null &&
                !vrijednost.isBlank() &&
                !vrijednost.equalsIgnoreCase("EMPTY");
    }
    @PostMapping("/registracija-profil")
    public Korisnik registracijaProfil(@RequestBody RegistracijaProfilRequest request) {

        if (request.getSupabaseUid() == null) {
            return null;
        }

        Korisnik korisnik = korisnikRepository
                .findBySupabaseUid(request.getSupabaseUid())
                .orElseGet(Korisnik::new);

        korisnik.setSupabaseUid(request.getSupabaseUid());
        korisnik.setEmail(request.getEmail());
        korisnik.setIme(request.getIme());
        korisnik.setPrezime(request.getPrezime());
        korisnik.setLozinka_hash("");

        Korisnik spremljeniKorisnik = korisnikRepository.save(korisnik);

        ProfilPodaci profil = profilPodaciRepository
                .findByIdKorisnik(spremljeniKorisnik.getId())
                .orElseGet(ProfilPodaci::new);

        profil.setIdKorisnik(spremljeniKorisnik.getId());
        profil.setSpol(request.getSpol());
        profil.setVisina(request.getVisina());
        profil.setDob(request.getDob());

        profilPodaciRepository.save(profil);

        Mjerenje mjerenje = new Mjerenje();
        mjerenje.setIdKorisnik(spremljeniKorisnik.getId());
        mjerenje.setMasa(BigDecimal.valueOf(request.getMasa()).setScale(1, RoundingMode.HALF_UP));
        mjerenje.setRazina_aktivnosti(request.getRazinaAktivnosti());
        mjerenje.setTip_cilja(request.getTipCilja());
        mjerenje.setDatum(LocalDate.now());

        mjerenjeRepository.save(mjerenje);

        izracunPreporukeService.izracunajPreporuku(spremljeniKorisnik.getId());

        return spremljeniKorisnik;
    }

}