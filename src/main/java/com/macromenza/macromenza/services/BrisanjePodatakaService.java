package com.macromenza.macromenza.services;

import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class BrisanjePodatakaService {

    private final KorisnikRepository korisnikRepository;
    private final ObrokRepository obrokRepository;
    private final KonzumacijaRepository konzumacijaRepository;
    private final MjerenjeRepository mjerenjeRepository;
    private final PreporukaRepository preporukaRepository;

    public BrisanjePodatakaService(
            KorisnikRepository korisnikRepository,
            ObrokRepository obrokRepository,
            KonzumacijaRepository konzumacijaRepository,
            MjerenjeRepository mjerenjeRepository,
            PreporukaRepository preporukaRepository
    ) {
        this.korisnikRepository = korisnikRepository;
        this.obrokRepository = obrokRepository;
        this.konzumacijaRepository = konzumacijaRepository;
        this.mjerenjeRepository = mjerenjeRepository;
        this.preporukaRepository = preporukaRepository;
    }

    /**
     * Briše obroke (konzumacije brišu se kaskadno u bazi), mjerenja i preporuke korisnika u jednoj transakciji.
     * Račun (korisnik) i profil_podaci ostaju. Vraća broj obrisanih redova po tablici.
     */
    @Transactional
    public Map<String, Long> obrisiSvePodatke(Long idKorisnik, String potvrda) {

        if (!"OBRISI".equals(potvrda)) {
            throw new IllegalArgumentException("Za brisanje je potrebna potvrda=OBRISI.");
        }

        if (!korisnikRepository.existsById(idKorisnik)) {
            throw new NoSuchElementException("Korisnik ne postoji.");
        }

        long konzumacije = konzumacijaRepository.countByIdKorisnik(idKorisnik);

        Map<String, Long> obrisano = new LinkedHashMap<>();
        obrisano.put("obrok", (long) obrokRepository.obrisiSveZaKorisnika(idKorisnik));
        obrisano.put("konzumacija", konzumacije);
        obrisano.put("mjerenje", (long) mjerenjeRepository.obrisiSveZaKorisnika(idKorisnik));
        obrisano.put("preporuka", (long) preporukaRepository.obrisiSveZaKorisnika(idKorisnik));
        return obrisano;
    }
}
