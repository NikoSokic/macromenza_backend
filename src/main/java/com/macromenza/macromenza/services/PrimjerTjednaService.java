package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.MeniDTO;
import com.macromenza.macromenza.dto.MeniStavkaDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Obrok;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Primjer tjedna: za zadnjih 7 dana (bez danas) koji nemaju nijedan obrok stvara doručak, ručak i večeru s
 * izvorom PRIMJER. Izbor je deterministički: isti ulaz (cilj, jela, meniji, zauzeti dani) daje isti rezultat.
 */
@Service
public class PrimjerTjednaService {

    public static final String IZVOR = "PRIMJER";
    private static final String KATEGORIJA_DORUCAK = "Brza jela i doručak";

    private final ObrokRepository obrokRepository;
    private final KonzumacijaRepository konzumacijaRepository;
    private final KorisnikRepository korisnikRepository;
    private final PreporukaRepository preporukaRepository;
    private final JeloRepository jeloRepository;
    private final JeloNutritivnoRepository jeloNutritivnoRepository;
    private final MeniService meniService;

    public PrimjerTjednaService(
            ObrokRepository obrokRepository,
            KonzumacijaRepository konzumacijaRepository,
            KorisnikRepository korisnikRepository,
            PreporukaRepository preporukaRepository,
            JeloRepository jeloRepository,
            JeloNutritivnoRepository jeloNutritivnoRepository,
            MeniService meniService
    ) {
        this.obrokRepository = obrokRepository;
        this.konzumacijaRepository = konzumacijaRepository;
        this.korisnikRepository = korisnikRepository;
        this.preporukaRepository = preporukaRepository;
        this.jeloRepository = jeloRepository;
        this.jeloNutritivnoRepository = jeloNutritivnoRepository;
        this.meniService = meniService;
    }

    /** Vraća broj stvorenih obroka po danu (samo dani koji su dobili primjer). */
    @Transactional
    public Map<String, Integer> ucitaj(Long idKorisnik) {

        if (!korisnikRepository.existsById(idKorisnik)) {
            throw new NoSuchElementException("Korisnik ne postoji.");
        }

        Preporuka preporuka = preporukaRepository.findTopByIdKorisnikOrderByIdDesc(idKorisnik)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nema dnevni cilj (preporuku)."));
        double cilj = preporuka.getKalorije();

        List<Jelo> dorucci = jeloRepository.findByAktivnoTrueOrderByNaziv().stream()
                .filter(j -> KATEGORIJA_DORUCAK.equals(j.getKategorija()))
                .sorted(Comparator.comparing(Jelo::getId))
                .toList();

        if (dorucci.isEmpty()) {
            throw new IllegalStateException("Nema jela za doručak.");
        }

        Map<Long, Double> kcalJela = jeloNutritivnoRepository
                .findAllById(dorucci.stream().map(Jelo::getId).toList()).stream()
                .collect(Collectors.toMap(JeloNutritivno::getIdJelo, n -> n.getKalorije().doubleValue()));

        List<MeniDTO> rucci = upotrebljiviMeniji("RUCAK");
        List<MeniDTO> vecere = upotrebljiviMeniji("VECERA");
        Set<Long> iskoristeni = new HashSet<>();

        LocalDate danas = LocalDate.now(ObrokService.ZAGREB);
        Map<String, Integer> stvoreno = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate dan = danas.minusDays(7 - i);

            if (obrokRepository.existsByIdKorisnikAndDatum(idKorisnik, dan)) {
                continue;
            }

            Jelo a = dorucci.get((2 * i) % dorucci.size());
            Jelo b = dorucci.get((2 * i + 1) % dorucci.size());
            List<List<Jelo>> opcije = List.of(List.of(a), List.of(a, b));

            double najboljeOdstupanje = Double.MAX_VALUE;
            List<Jelo> dorucak = null;
            MeniDTO rucak = null;
            MeniDTO vecera = null;

            for (List<Jelo> opcija : opcije) {
                double kcalDorucka = opcija.stream().mapToDouble(j -> kcalJela.getOrDefault(j.getId(), 0.0)).sum();

                for (MeniDTO r : rucci) {
                    if (iskoristeni.contains(r.getId())) continue;

                    for (MeniDTO v : vecere) {
                        if (iskoristeni.contains(v.getId())) continue;

                        double odstupanje = Math.abs(kcalDorucka + r.getKalorije().doubleValue()
                                + v.getKalorije().doubleValue() - cilj);

                        if (odstupanje < najboljeOdstupanje) {
                            najboljeOdstupanje = odstupanje;
                            dorucak = opcija;
                            rucak = r;
                            vecera = v;
                        }
                    }
                }
            }

            if (rucak == null || vecera == null) {
                break; // nema više neiskorištenih menija
            }

            iskoristeni.add(rucak.getId());
            iskoristeni.add(vecera.getId());

            spremi(idKorisnik, dan, "DORUCAK", LocalTime.of(8, 0), dorucak.stream()
                    .map(j -> new StavkaPrimjera(j.getId(), BigDecimal.ONE)).toList());
            spremi(idKorisnik, dan, "RUCAK", LocalTime.of(13, 0), stavkeMenija(rucak));
            spremi(idKorisnik, dan, "VECERA", LocalTime.of(19, 0), stavkeMenija(vecera));

            stvoreno.put(dan.toString(), 3);
        }

        return stvoreno;
    }

    /** Briše samo obroke s izvorom PRIMJER. */
    @Transactional
    public int ukloni(Long idKorisnik) {
        return obrokRepository.obrisiPoIzvoru(idKorisnik, IZVOR);
    }

    /** Meniji tipa čije su sve količine višekratnici od 0,5 i koji imaju vrijednosti; poredani po id-u. */
    private List<MeniDTO> upotrebljiviMeniji(String tip) {
        return meniService.dohvatiMenije(tip, null).stream()
                .filter(m -> m.getKalorije() != null && !m.getStavke().isEmpty())
                .filter(m -> m.getStavke().stream().allMatch(s -> jePolovica(s.getKolicina())))
                .sorted(Comparator.comparing(MeniDTO::getId))
                .toList();
    }

    private boolean jePolovica(BigDecimal kolicina) {
        return kolicina != null && kolicina.signum() > 0
                && kolicina.multiply(BigDecimal.valueOf(2)).stripTrailingZeros().scale() <= 0;
    }

    private List<StavkaPrimjera> stavkeMenija(MeniDTO meni) {
        List<StavkaPrimjera> stavke = new ArrayList<>();
        for (MeniStavkaDTO s : meni.getStavke()) {
            stavke.add(new StavkaPrimjera(s.getIdJelo(), s.getKolicina()));
        }
        return stavke;
    }

    private void spremi(Long idKorisnik, LocalDate datum, String tip, LocalTime vrijeme, List<StavkaPrimjera> stavke) {
        Obrok obrok = new Obrok();
        obrok.setIdKorisnik(idKorisnik);
        obrok.setDatum(datum);
        obrok.setVrijeme(vrijeme);
        obrok.setTipObroka(tip);
        obrok.setIzvor(IZVOR);
        obrok = obrokRepository.save(obrok);

        List<Konzumacija> konzumacije = new ArrayList<>();
        for (StavkaPrimjera s : stavke) {
            Konzumacija k = new Konzumacija();
            k.setIdKorisnik(idKorisnik);
            k.setIdJelo(s.idJelo());
            k.setIdObrok(obrok.getId());
            k.setDatum(datum);
            k.setTip_obroka(tip);
            k.setKolicina(s.kolicina());
            konzumacije.add(k);
        }
        konzumacijaRepository.saveAllAndFlush(konzumacije);
    }

    private record StavkaPrimjera(Long idJelo, BigDecimal kolicina) {
    }
}
