package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Korisnik;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import org.springframework.web.bind.annotation.*;
import com.macromenza.macromenza.dto.RegistracijaProfilRequest;
import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;
import com.macromenza.macromenza.security.PristupZabranjenException;
import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.services.IzracunPreporukeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/korisnici")
public class KorisnikController {

    private final KorisnikRepository korisnikRepository;
    private final MjerenjeRepository mjerenjeRepository;
    private final ProfilPodaciRepository profilPodaciRepository;
    private final IzracunPreporukeService izracunPreporukeService;
    private final TrenutniKorisnik trenutni;

    public KorisnikController(
            KorisnikRepository korisnikRepository,
            MjerenjeRepository mjerenjeRepository,
            ProfilPodaciRepository profilPodaciRepository,
            IzracunPreporukeService izracunPreporukeService,
            TrenutniKorisnik trenutni
    ) {
        this.korisnikRepository = korisnikRepository;
        this.mjerenjeRepository = mjerenjeRepository;
        this.profilPodaciRepository = profilPodaciRepository;
        this.izracunPreporukeService = izracunPreporukeService;
        this.trenutni = trenutni;
    }

    @GetMapping("/{id}")
    public Korisnik getKorisnikPoId(@PathVariable Long id) {
        trenutni.provjeri(id);
        return korisnikRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Korisnik ne postoji."));
    }

    /** Pronalazi korisnika prijavljenog tokenom ili ga stvara; uid dolazi iz tokena, a ne iz tijela zahtjeva. */
    @PostMapping("/supabase")
    public Korisnik pronadiIliDodajKorisnikaPrekoSupabase(
            @RequestBody Korisnik noviKorisnik
    ) {
        UUID uid = trenutni.supabaseUid();
        if (noviKorisnik.getSupabaseUid() != null && !uid.equals(noviKorisnik.getSupabaseUid())) {
            throw new PristupZabranjenException("Token ne pripada traženom korisniku.");
        }
        noviKorisnik.setSupabaseUid(uid);

        return korisnikRepository
                .findBySupabaseUid(uid)
                .orElseGet(() -> korisnikRepository.save(noviKorisnik));
    }

    @PutMapping("/{id}")
    public Korisnik azurirajKorisnika(
            @PathVariable Long id,
            @RequestBody Korisnik noviKorisnik
    ) {
        trenutni.provjeri(id);
        Korisnik korisnik = korisnikRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Korisnik ne postoji."));

        korisnik.setIme(noviKorisnik.getIme());
        korisnik.setPrezime(noviKorisnik.getPrezime());
        korisnik.setEmail(noviKorisnik.getEmail());

        return korisnikRepository.save(korisnik);
    }

    @GetMapping("/{id}/onboarding-status")
    public Map<String, Boolean> getOnboardingStatus(@PathVariable Long id) {
        trenutni.provjeri(id);

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

        UUID uid = trenutni.supabaseUid();
        if (request.getSupabaseUid() != null && !uid.equals(request.getSupabaseUid())) {
            throw new PristupZabranjenException("Token ne pripada traženom korisniku.");
        }

        Korisnik korisnik = korisnikRepository
                .findBySupabaseUid(uid)
                .orElseGet(Korisnik::new);

        korisnik.setSupabaseUid(uid);
        korisnik.setEmail(request.getEmail());
        korisnik.setIme(request.getIme());
        korisnik.setPrezime(request.getPrezime());

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