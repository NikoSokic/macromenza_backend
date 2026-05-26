package com.macromenza.macromenza.services;

import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import com.macromenza.macromenza.repository.PreporukaRepository;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class IzracunPreporukeService {

    private final MjerenjeRepository mjerenjeRepository;
    private final ProfilPodaciRepository profilPodaciRepository;
    private final PreporukaRepository preporukaRepository;

    public IzracunPreporukeService(
            MjerenjeRepository mjerenjeRepository,
            ProfilPodaciRepository profilPodaciRepository,
            PreporukaRepository preporukaRepository
    ) {
        this.mjerenjeRepository = mjerenjeRepository;
        this.profilPodaciRepository = profilPodaciRepository;
        this.preporukaRepository = preporukaRepository;
    }

    public Preporuka izracunajPreporuku(Long korisnikId) {

        Mjerenje mjerenje = mjerenjeRepository
                .findTopByIdKorisnikOrderByDatumDesc(korisnikId)
                .orElseThrow(() -> new RuntimeException("Korisnik nema mjerenje"));

        ProfilPodaci profil = profilPodaciRepository
                .findByIdKorisnik(korisnikId)
                .orElseThrow(() -> new RuntimeException("Korisnik nema profil podatke"));

        double masa = mjerenje.getMasa();
        int visina = profil.getVisina();
        int dob = profil.getDob();
        String spol = profil.getSpol();

        double bmr;

        if (spol.equalsIgnoreCase("M")) {
            bmr = 10 * masa + 6.25 * visina - 5 * dob + 5;
        } else {
            bmr = 10 * masa + 6.25 * visina - 5 * dob - 161;
        }

        double faktorAktivnosti = odrediFaktorAktivnosti(mjerenje.getRazina_aktivnosti());

        double kalorije = bmr * faktorAktivnosti;

        kalorije = prilagodiKalorijePremaCilju(kalorije, mjerenje.getTip_cilja());

        double proteini = masa * 2;
        double masti = (kalorije * 0.25) / 9;
        double ugljikohidrati = (kalorije - proteini * 4 - masti * 9) / 4;

        Preporuka preporuka = new Preporuka();

        preporuka.setIdKorisnik(mjerenje.getIdKorisnik());
        preporuka.setKalorije((int) Math.round(kalorije));
        preporuka.setProteini((int) Math.round(proteini));
        preporuka.setUgljikohidrati((int) Math.round(ugljikohidrati));
        preporuka.setMasti((int) Math.round(masti));
        preporuka.setDatum(LocalDate.now());

        return preporukaRepository.save(preporuka);
    }

    private double odrediFaktorAktivnosti(String razinaAktivnosti) {
        return switch (razinaAktivnosti.toLowerCase()) {
            case "sjedilacka" -> 1.2;
            case "lagana" -> 1.375;
            case "umjerena" -> 1.55;
            case "visoka" -> 1.725;
            default -> 1.2;
        };
    }

    private double prilagodiKalorijePremaCilju(double kalorije, String tipCilja) {
        return switch (tipCilja.toLowerCase()) {
            case "mrsavljenje" -> kalorije - 500;
            case "dobivanje_mase" -> kalorije + 300;
            case "odrzavanje" -> kalorije;
            default -> kalorije;
        };
    }
    public Preporuka dohvatiZadnjuPreporuku(Long korisnikId) {

        return preporukaRepository
                .findTopByIdKorisnikOrderByIdDesc(korisnikId)
                .orElseGet(() -> izracunajPreporuku(korisnikId));
    }


}