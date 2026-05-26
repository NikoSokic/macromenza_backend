package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.PovijestDanaDTO;
import com.macromenza.macromenza.dto.StavkaObrokaDTO;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                konzumacijaRepository.findByIdKorisnikAndDatum(
                        idKorisnik,
                        LocalDate.now()
                );

        UkupniUnosDTO rezultat = new UkupniUnosDTO();

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

    public List<LocalDate> dohvatiPovijestDana(Long idKorisnik) {

        return konzumacijaRepository
                .findByIdKorisnikOrderByDatumDesc(idKorisnik)
                .stream()
                .map(Konzumacija::getDatum)
                .distinct()
                .toList();
    }

    public PovijestDanaDTO dohvatiPovijestZaDan(
            Long idKorisnik,
            LocalDate datum
    ) {

        List<Konzumacija> lista =
                konzumacijaRepository.findByIdKorisnikAndDatum(
                        idKorisnik,
                        datum
                );

        List<StavkaObrokaDTO> stavke =
                lista.stream()
                        .map(k -> {

                            Jelo jelo =
                                    jeloRepository
                                            .findById(k.getIdJelo())
                                            .orElseThrow();

                            return new StavkaObrokaDTO(
                                    jelo.getNaziv(),
                                    k.getTip_obroka(),
                                    k.getKolicina(),
                                    jelo.getKalorije() * k.getKolicina(),
                                    jelo.getProteini() * k.getKolicina(),
                                    jelo.getUgljikohidrati() * k.getKolicina(),
                                    jelo.getMasti() * k.getKolicina()
                            );
                        })
                        .toList();

        int kalorije =
                stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getKalorije)
                        .sum();

        int proteini =
                stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getProteini)
                        .sum();

        int ugljikohidrati =
                stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getUgljikohidrati)
                        .sum();

        int masti =
                stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getMasti)
                        .sum();

        Map<String, List<StavkaObrokaDTO>> obroci =
                stavke.stream()
                        .collect(
                                Collectors.groupingBy(
                                        StavkaObrokaDTO::getTipObroka
                                )
                        );

        return new PovijestDanaDTO(
                datum,
                kalorije,
                proteini,
                ugljikohidrati,
                masti,
                obroci
        );
    }
}