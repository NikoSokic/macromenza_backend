package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.ObrokDTO;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.dto.PovijestDanaDTO;
import com.macromenza.macromenza.dto.StavkaObrokaDTO;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.model.Obrok;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.dto.DnevniPregledDTO;
import com.macromenza.macromenza.dto.TjedniPregledDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class KonzumacijaService {

    private final KonzumacijaRepository konzumacijaRepository;
    private final JeloRepository jeloRepository;
    private final JeloNutritivnoRepository jeloNutritivnoRepository;
    private final ObrokRepository obrokRepository;
    private final ObrokService obrokService;

    public KonzumacijaService(
            KonzumacijaRepository konzumacijaRepository,
            JeloRepository jeloRepository,
            JeloNutritivnoRepository jeloNutritivnoRepository,
            ObrokRepository obrokRepository,
            ObrokService obrokService
    ) {
        this.konzumacijaRepository = konzumacijaRepository;
        this.jeloRepository = jeloRepository;
        this.jeloNutritivnoRepository = jeloNutritivnoRepository;
        this.obrokRepository = obrokRepository;
        this.obrokService = obrokService;
    }

    /** Zastarjelo: stvara obrok s jednom stavkom. Novi unos ide preko POST /api/obroci. */
    @Deprecated
    public Konzumacija dodajKonzumaciju(Konzumacija konzumacija) {

        if (konzumacija.getIdKorisnik() == null) {
            throw new RuntimeException("Korisnik je obavezan");
        }

        if (konzumacija.getIdJelo() == null) {
            throw new RuntimeException("Jelo je obavezno");
        }

        if (konzumacija.getKolicina() == null || konzumacija.getKolicina().signum() <= 0) {
            throw new RuntimeException("Kolicina mora biti veca od 0");
        }

        if (konzumacija.getTip_obroka() == null || konzumacija.getTip_obroka().isBlank()) {
            throw new RuntimeException("Tip obroka je obavezan");
        }

        ObrokRequest request = new ObrokRequest();
        request.setIdKorisnik(konzumacija.getIdKorisnik());
        request.setDatum(konzumacija.getDatum() != null ? konzumacija.getDatum() : LocalDate.now(ObrokService.ZAGREB));
        request.setVrijeme(LocalTime.now(ObrokService.ZAGREB));
        request.setTipObroka(konzumacija.getTip_obroka().toUpperCase());
        request.setIzvor("RUCNO");
        request.setStavke(List.of(new ObrokRequest.Stavka(konzumacija.getIdJelo(), konzumacija.getKolicina())));

        ObrokDTO obrok;
        try {
            obrok = obrokService.spremiObrok(request);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }

        return konzumacijaRepository.findByIdObrokOrderByIdAsc(obrok.getId()).get(0);
    }

    public UkupniUnosDTO izracunajDnevniUnos(Long idKorisnik) {

        List<Konzumacija> konzumacije = konzumacijaRepository.findZaDan(idKorisnik, LocalDate.now());

        UkupniUnosDTO rezultat = new UkupniUnosDTO();

        BigDecimal kalorije = BigDecimal.ZERO;
        BigDecimal proteini = BigDecimal.ZERO;
        BigDecimal ugljikohidrati = BigDecimal.ZERO;
        BigDecimal masti = BigDecimal.ZERO;

        for (Konzumacija k : konzumacije) {

            JeloNutritivno jelo = jeloNutritivnoRepository.findById(k.getIdJelo()).orElseThrow();
            BigDecimal kolicina = k.getKolicina();

            kalorije = kalorije.add(jelo.getKalorije().multiply(kolicina));
            proteini = proteini.add(jelo.getProteini().multiply(kolicina));
            ugljikohidrati = ugljikohidrati.add(jelo.getUgljikohidrati().multiply(kolicina));
            masti = masti.add(jelo.getMasti().multiply(kolicina));
        }

        rezultat.setKalorije(kalorije);
        rezultat.setProteini(proteini);
        rezultat.setUgljikohidrati(ugljikohidrati);
        rezultat.setMasti(masti);

        return rezultat;
    }

    public List<LocalDate> dohvatiPovijestDana(Long idKorisnik) {

        return obrokRepository.findDatumiZaKorisnika(idKorisnik);
    }

    public PovijestDanaDTO dohvatiPovijestZaDan(
            Long idKorisnik,
            LocalDate datum
    ) {

        List<Konzumacija> lista = konzumacijaRepository.findZaDan(idKorisnik, datum);

        List<StavkaObrokaDTO> stavke =
                lista.stream()
                        .map(k -> {

                            Jelo jelo = jeloRepository
                                    .findById(k.getIdJelo())
                                    .orElseThrow();

                            JeloNutritivno nutritivno = jeloNutritivnoRepository
                                    .findById(k.getIdJelo())
                                    .orElseThrow();
                            BigDecimal kolicina = k.getKolicina();

                            return new StavkaObrokaDTO(
                                    jelo.getNaziv(),
                                    k.getTip_obroka(),
                                    k.getKolicina(),
                                    nutritivno.getKalorije().multiply(kolicina),
                                    nutritivno.getProteini().multiply(kolicina),
                                    nutritivno.getUgljikohidrati().multiply(kolicina),
                                    nutritivno.getMasti().multiply(kolicina)
                            );
                        })
                        .toList();

        BigDecimal kalorije = stavke.stream()
                        .map(StavkaObrokaDTO::getKalorije)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal proteini = stavke.stream()
                        .map(StavkaObrokaDTO::getProteini)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ugljikohidrati = stavke.stream()
                        .map(StavkaObrokaDTO::getUgljikohidrati)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal masti = stavke.stream()
                        .map(StavkaObrokaDTO::getMasti)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

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

        Map<Long, LocalDate> datumObroka = obrokRepository
                .findByIdKorisnikAndDatumBetween(idKorisnik, datumOd, datumDo)
                .stream()
                .collect(Collectors.toMap(Obrok::getId, Obrok::getDatum));

        List<Konzumacija> konzumacije = konzumacijaRepository.findByIdObrokIn(datumObroka.keySet());

        List<DnevniPregledDTO> dani = new ArrayList<>();

        BigDecimal ukupnoKalorije = BigDecimal.ZERO;
        BigDecimal ukupnoProteini = BigDecimal.ZERO;
        BigDecimal ukupnoUgljikohidrati = BigDecimal.ZERO;
        BigDecimal ukupnoMasti = BigDecimal.ZERO;

        for (LocalDate datum = datumOd; !datum.isAfter(datumDo); datum = datum.plusDays(1)) {

            BigDecimal kalorijeDan = BigDecimal.ZERO;
            BigDecimal proteiniDan = BigDecimal.ZERO;
            BigDecimal ugljikohidratiDan = BigDecimal.ZERO;
            BigDecimal mastiDan = BigDecimal.ZERO;

            for (Konzumacija k : konzumacije) {

                if (datumObroka.get(k.getIdObrok()).equals(datum)) {

                    JeloNutritivno jelo = jeloNutritivnoRepository.findById(k.getIdJelo()).orElseThrow();
                    BigDecimal kolicina = k.getKolicina();

                    kalorijeDan = kalorijeDan.add(jelo.getKalorije().multiply(kolicina));
                    proteiniDan = proteiniDan.add(jelo.getProteini().multiply(kolicina));
                    ugljikohidratiDan = ugljikohidratiDan.add(jelo.getUgljikohidrati().multiply(kolicina));
                    mastiDan = mastiDan.add(jelo.getMasti().multiply(kolicina));
                }
            }

            ukupnoKalorije = ukupnoKalorije.add(kalorijeDan);
            ukupnoProteini = ukupnoProteini.add(proteiniDan);
            ukupnoUgljikohidrati = ukupnoUgljikohidrati.add(ugljikohidratiDan);
            ukupnoMasti = ukupnoMasti.add(mastiDan);

            dani.add(new DnevniPregledDTO(datum, kalorijeDan, proteiniDan, ugljikohidratiDan, mastiDan));
        }

        double prosjekKalorija = ukupnoKalorije.doubleValue() / 7.0;

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

        if (novaKonzumacija.getKolicina() == null || novaKonzumacija.getKolicina().signum() <= 0) {
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