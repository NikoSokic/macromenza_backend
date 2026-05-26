package com.macromenza.macromenza.services;

import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
public class KonzumacijaService {

    private final KonzumacijaRepository konzumacijaRepository;
    private final JeloRepository jeloRepository;

    public KonzumacijaService(
            KonzumacijaRepository konzumacijaRepository,
            JeloRepository jeloRepository
    ) {
        this.konzumacijaRepository = konzumacijaRepository;
        this.jeloRepository = jeloRepository;
    }

    public Konzumacija dodajKonzumaciju(Konzumacija konzumacija) {

        Jelo jelo = jeloRepository.findById(konzumacija.getIdJelo())
                .orElseThrow(() -> new RuntimeException("Jelo ne postoji"));

        konzumacija.setDatum(LocalDate.now());

        return konzumacijaRepository.save(konzumacija);
    }
    public UkupniUnosDTO izracunajDnevniUnos(Long idKorisnik) {

        List<Konzumacija> konzumacije =
                konzumacijaRepository
                        .findByIdKorisnikAndDatum(
                                idKorisnik,
                                LocalDate.now()
                        );

        UkupniUnosDTO rezultat =
                new UkupniUnosDTO();

        int kalorije = 0;
        int proteini = 0;
        int ugljikohidrati = 0;
        int masti = 0;

        for (Konzumacija k : konzumacije) {

            Jelo jelo = jeloRepository
                    .findById(k.getIdJelo())
                    .orElseThrow();

            kalorije += jelo.getKalorije() * k.getKolicina();

            proteini += jelo.getProteini() * k.getKolicina();

            ugljikohidrati += jelo.getUgljikohidrati() * k.getKolicina();

            masti += jelo.getMasti() * k.getKolicina();
        }

        rezultat.setKalorije(kalorije);
        rezultat.setProteini(proteini);
        rezultat.setUgljikohidrati(ugljikohidrati);
        rezultat.setMasti(masti);

        return rezultat;
    }
}