package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.PovijestDanaDTO;
import com.macromenza.macromenza.dto.StavkaObrokaDTO;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.dto.DnevniPregledDTO;
import com.macromenza.macromenza.dto.TjedniPregledDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

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

        if (konzumacija.getIdKorisnik() == null) {
            throw new RuntimeException("Korisnik je obavezan");
        }

        if (konzumacija.getIdJelo() == null) {
            throw new RuntimeException("Jelo je obavezno");
        }

        if (konzumacija.getKolicina() == null || konzumacija.getKolicina() <= 0) {
            throw new RuntimeException("Kolicina mora biti veca od 0");
        }

        if (konzumacija.getTip_obroka() == null || konzumacija.getTip_obroka().isBlank()) {
            throw new RuntimeException("Tip obroka je obavezan");
        }

        Jelo jelo = jeloRepository.findById(konzumacija.getIdJelo()).orElseThrow(() -> new RuntimeException("Jelo ne postoji"));

        konzumacija.setDatum(LocalDate.now());

        return konzumacijaRepository.save(konzumacija);
    }

    public UkupniUnosDTO izracunajDnevniUnos(Long idKorisnik) {

        List<Konzumacija> konzumacije = konzumacijaRepository.findByIdKorisnikAndDatum(idKorisnik, LocalDate.now());

        UkupniUnosDTO rezultat = new UkupniUnosDTO();

        int kalorije = 0;
        int proteini = 0;
        int ugljikohidrati = 0;
        int masti = 0;

        for (Konzumacija k : konzumacije) {

            Jelo jelo = jeloRepository.findById(k.getIdJelo()).orElseThrow();

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

        List<Konzumacija> lista = konzumacijaRepository.findByIdKorisnikAndDatum(idKorisnik, datum);

        List<StavkaObrokaDTO> stavke =
                lista.stream()
                        .map(k -> {

                            Jelo jelo = jeloRepository
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

        int kalorije = stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getKalorije)
                        .sum();

        int proteini = stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getProteini)
                        .sum();

        int ugljikohidrati = stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getUgljikohidrati)
                        .sum();

        int masti = stavke.stream()
                        .mapToInt(StavkaObrokaDTO::getMasti)
                        .sum();

        Map<String, List<StavkaObrokaDTO>> obroci = stavke.stream()
                        .collect(Collectors.groupingBy(StavkaObrokaDTO::getTipObroka)
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
    public TjedniPregledDTO dohvatiTjedniPregled(Long idKorisnik) {

        LocalDate datumDo = LocalDate.now();
        LocalDate datumOd = datumDo.minusDays(6);

        List<Konzumacija> konzumacije = konzumacijaRepository.findByIdKorisnikAndDatumBetween(idKorisnik, datumOd, datumDo);

        List<DnevniPregledDTO> dani = new ArrayList<>();

        int ukupnoKalorije = 0;
        int ukupnoProteini = 0;
        int ukupnoUgljikohidrati = 0;
        int ukupnoMasti = 0;

        for (LocalDate datum = datumOd; !datum.isAfter(datumDo); datum = datum.plusDays(1)) {

            int kalorijeDan = 0;
            int proteiniDan = 0;
            int ugljikohidratiDan = 0;
            int mastiDan = 0;

            for (Konzumacija k : konzumacije) {

                if (k.getDatum().equals(datum)) {

                    Jelo jelo = jeloRepository.findById(k.getIdJelo()).orElseThrow();

                    kalorijeDan += jelo.getKalorije() * k.getKolicina();
                    proteiniDan += jelo.getProteini() * k.getKolicina();
                    ugljikohidratiDan += jelo.getUgljikohidrati() * k.getKolicina();
                    mastiDan += jelo.getMasti() * k.getKolicina();
                }
            }

            ukupnoKalorije += kalorijeDan;
            ukupnoProteini += proteiniDan;
            ukupnoUgljikohidrati += ugljikohidratiDan;
            ukupnoMasti += mastiDan;

            dani.add(new DnevniPregledDTO(datum, kalorijeDan, proteiniDan, ugljikohidratiDan, mastiDan));
        }

        double prosjekKalorija = ukupnoKalorije / 7.0;

        return new TjedniPregledDTO(
                datumOd,
                datumDo,
                ukupnoKalorije,
                ukupnoProteini,
                ukupnoUgljikohidrati,
                ukupnoMasti,
                prosjekKalorija,
                dani
        );
    }
    public Konzumacija azurirajKonzumaciju(
            Long id,
            Konzumacija novaKonzumacija
    ) {
        Konzumacija postojecaKonzumacija = konzumacijaRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Konzumacija ne postoji"));

        if (novaKonzumacija.getIdJelo() == null) {
            throw new RuntimeException("Jelo je obavezno");
        }

        if (novaKonzumacija.getKolicina() == null || novaKonzumacija.getKolicina() <= 0) {
            throw new RuntimeException("Kolicina mora biti veca od 0");
        }

        if (novaKonzumacija.getTip_obroka() == null || novaKonzumacija.getTip_obroka().isBlank()) {
            throw new RuntimeException("Tip obroka je obavezan");
        }

        Jelo jelo = jeloRepository
                .findById(novaKonzumacija.getIdJelo())
                .orElseThrow(() -> new RuntimeException("Jelo ne postoji"));

        postojecaKonzumacija.setIdJelo(novaKonzumacija.getIdJelo());
        postojecaKonzumacija.setKolicina(novaKonzumacija.getKolicina());
        postojecaKonzumacija.setTip_obroka(novaKonzumacija.getTip_obroka());

        return konzumacijaRepository.save(postojecaKonzumacija);
    }
}