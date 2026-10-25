package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.DanPregledDTO;
import com.macromenza.macromenza.dto.NutritivneVrijednostiDTO;
import com.macromenza.macromenza.dto.ObrokDTO;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Obrok;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository.StavkaObrokaRed;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.repository.ObrokRepository.TipObrokaRed;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ObrokService {

    public static final ZoneId ZAGREB = ZoneId.of("Europe/Zagreb");

    private static final List<String> REDOSLIJED_TIPOVA = List.of("DORUCAK", "RUCAK", "UZINA", "VECERA");
    private static final Set<String> TIPOVI = Set.copyOf(REDOSLIJED_TIPOVA);
    private static final Set<String> IZVORI = Set.of("RUCNO", "MENI", "SLIKA", "OPIS");
    private static final BigDecimal NAJVISE_PORCIJA = new BigDecimal("99.5");
    private static final DateTimeFormatter FORMAT_VREMENA = DateTimeFormatter.ofPattern("HH:mm");

    private final ObrokRepository obrokRepository;
    private final KonzumacijaRepository konzumacijaRepository;
    private final JeloRepository jeloRepository;
    private final KorisnikRepository korisnikRepository;
    private final PreporukaRepository preporukaRepository;
    private final SavjetService savjetService;

    public ObrokService(
            ObrokRepository obrokRepository,
            KonzumacijaRepository konzumacijaRepository,
            JeloRepository jeloRepository,
            KorisnikRepository korisnikRepository,
            PreporukaRepository preporukaRepository,
            SavjetService savjetService
    ) {
        this.obrokRepository = obrokRepository;
        this.konzumacijaRepository = konzumacijaRepository;
        this.jeloRepository = jeloRepository;
        this.korisnikRepository = korisnikRepository;
        this.preporukaRepository = preporukaRepository;
        this.savjetService = savjetService;
    }

    /** Sprema obrok i sve njegove stavke u jednoj transakciji (sve ili ništa). */
    @Transactional
    public ObrokDTO spremiObrok(ObrokRequest request) {

        String izvor = izvor(request);
        provjeri(request, izvor, Set.of());

        Obrok obrok = new Obrok();
        obrok.setIdKorisnik(request.getIdKorisnik());
        postaviPodatke(obrok, request, izvor);
        obrok = obrokRepository.save(obrok);

        upisiStavke(obrok, request.getStavke());

        return mapiraj(List.of(obrok)).get(0);
    }

    public ObrokDTO dohvatiObrok(Long id, Long idKorisnik) {
        return mapiraj(List.of(nadiObrok(id, idKorisnik))).get(0);
    }

    /**
     * Mijenja obrok i zamjenjuje sve njegove stavke u jednoj transakciji (sve ili ništa).
     * Konzumacije dobivaju datum, tip i korisnika iz obroka, pa ostaju usklađene s njim.
     */
    @Transactional
    public ObrokDTO azurirajObrok(Long id, Long idKorisnik, ObrokRequest request) {

        // Vlasništvo se provjerava prije validacije: tuđi obrok uvijek daje 404.
        Obrok obrok = nadiObrok(id, idKorisnik);
        List<Konzumacija> stare = konzumacijaRepository.findByIdObrokOrderByIdAsc(id);

        request.setIdKorisnik(idKorisnik);
        String izvor = izvor(request);
        // Jelo koje je već u obroku smije ostati i ako je u međuvremenu deaktivirano.
        provjeri(request, izvor, stare.stream().map(Konzumacija::getIdJelo).collect(Collectors.toSet()));

        postaviPodatke(obrok, request, izvor);
        obrok = obrokRepository.save(obrok);

        konzumacijaRepository.deleteAllInBatch(stare);
        upisiStavke(obrok, request.getStavke());

        return mapiraj(List.of(obrok)).get(0);
    }

    /** Briše obrok; njegove konzumacije briše baza (konzumacija.id_obrok ima ON DELETE CASCADE). */
    @Transactional
    public void obrisiObrok(Long id, Long idKorisnik) {
        obrokRepository.delete(nadiObrok(id, idKorisnik));
    }

    public List<ObrokDTO> dohvatiObroke(Long idKorisnik, LocalDate datum) {
        return mapiraj(obrokRepository.findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(idKorisnik, datum));
    }

    /**
     * Pregled dana za početni ekran: cilj (zadnja preporuka s datumom <= datum), pojedeno, kalorije po tipu obroka
     * i savjet dana. Totali se računaju jednim upitom; [datum] zadano je danas (Europe/Zagreb).
     */
    public DanPregledDTO dohvatiDanPregled(Long idKorisnik, LocalDate datum) {

        if (!korisnikRepository.existsById(idKorisnik)) {
            throw new NoSuchElementException("Korisnik ne postoji.");
        }

        LocalDate danas = LocalDate.now(ZAGREB);
        LocalDate trazeniDatum = datum == null ? danas : datum;

        Map<String, TipObrokaRed> redovi = obrokRepository.findTotaliPoTipu(idKorisnik, trazeniDatum).stream()
                .collect(Collectors.toMap(TipObrokaRed::getTipObroka, Function.identity()));

        List<DanPregledDTO.PoTipu> poTipu = new ArrayList<>();
        BigDecimal kalorije = BigDecimal.ZERO;
        BigDecimal proteini = BigDecimal.ZERO;
        BigDecimal ugljikohidrati = BigDecimal.ZERO;
        BigDecimal masti = BigDecimal.ZERO;

        // Pojedeno je zbroj već zaokruženih vrijednosti, pa zbroj poTipu.kalorije uvijek jednak pojedeno.kalorije.
        for (String tip : REDOSLIJED_TIPOVA) {
            TipObrokaRed red = redovi.get(tip);

            if (red == null) {
                poTipu.add(new DanPregledDTO.PoTipu(tip, BigDecimal.ZERO.setScale(2), 0));
                continue;
            }

            BigDecimal kalorijeTipa = zaokruzi(red.getKalorije());
            poTipu.add(new DanPregledDTO.PoTipu(tip, kalorijeTipa, red.getBrojObroka()));

            kalorije = kalorije.add(kalorijeTipa);
            proteini = proteini.add(zaokruzi(red.getProteini()));
            ugljikohidrati = ugljikohidrati.add(zaokruzi(red.getUgljikohidrati()));
            masti = masti.add(zaokruzi(red.getMasti()));
        }

        NutritivneVrijednostiDTO pojedeno = new NutritivneVrijednostiDTO(kalorije, proteini, ugljikohidrati, masti);

        NutritivneVrijednostiDTO cilj = preporukaRepository
                .findTopByIdKorisnikAndDatumLessThanEqualOrderByDatumDescIdDesc(idKorisnik, trazeniDatum)
                .map(p -> new NutritivneVrijednostiDTO(
                        BigDecimal.valueOf(p.getKalorije()),
                        BigDecimal.valueOf(p.getProteini()),
                        BigDecimal.valueOf(p.getUgljikohidrati()),
                        BigDecimal.valueOf(p.getMasti())
                ))
                .orElse(null);

        DanPregledDTO.Savjet savjet = savjetService.odaberi(
                cilj, pojedeno, trazeniDatum, danas, LocalTime.now(ZAGREB)
        );

        return new DanPregledDTO(cilj, pojedeno, poTipu, savjet);
    }

    private BigDecimal zaokruzi(BigDecimal vrijednost) {
        return vrijednost.setScale(2, RoundingMode.HALF_UP);
    }

    private Obrok nadiObrok(Long id, Long idKorisnik) {
        return obrokRepository.findByIdAndIdKorisnik(id, idKorisnik)
                .orElseThrow(() -> new NoSuchElementException("Obrok ne postoji."));
    }

    private String izvor(ObrokRequest request) {
        return request.getIzvor() == null ? "RUCNO" : request.getIzvor();
    }

    private void postaviPodatke(Obrok obrok, ObrokRequest request, String izvor) {
        obrok.setDatum(request.getDatum());
        obrok.setVrijeme(request.getVrijeme().truncatedTo(ChronoUnit.MINUTES));
        obrok.setTipObroka(request.getTipObroka());
        obrok.setIzvor(izvor);
    }

    private void upisiStavke(Obrok obrok, List<ObrokRequest.Stavka> stavke) {
        List<Konzumacija> konzumacije = new ArrayList<>();
        for (ObrokRequest.Stavka stavka : stavke) {
            Konzumacija k = new Konzumacija();
            k.setIdKorisnik(obrok.getIdKorisnik());
            k.setIdJelo(stavka.getIdJelo());
            k.setIdObrok(obrok.getId());
            k.setDatum(obrok.getDatum());
            k.setTip_obroka(obrok.getTipObroka());
            k.setKolicina(stavka.getKolicina());
            konzumacije.add(k);
        }
        konzumacijaRepository.saveAllAndFlush(konzumacije);
    }

    /** [dopustenaNeaktivna]: jela koja smiju biti neaktivna (ona koja su već bila u obroku koji se uređuje). */
    private void provjeri(ObrokRequest request, String izvor, Set<Long> dopustenaNeaktivna) {

        if (request.getIdKorisnik() == null) {
            throw new IllegalArgumentException("Korisnik je obavezan.");
        }

        if (!korisnikRepository.existsById(request.getIdKorisnik())) {
            throw new IllegalArgumentException("Korisnik ne postoji.");
        }

        if (request.getDatum() == null) {
            throw new IllegalArgumentException("Datum je obavezan.");
        }

        if (request.getDatum().isAfter(LocalDate.now(ZAGREB))) {
            throw new IllegalArgumentException("Datum obroka ne može biti u budućnosti.");
        }

        if (request.getVrijeme() == null) {
            throw new IllegalArgumentException("Vrijeme je obavezno.");
        }

        if (request.getTipObroka() == null || !TIPOVI.contains(request.getTipObroka())) {
            throw new IllegalArgumentException("Tip obroka mora biti DORUCAK, RUCAK, UZINA ili VECERA.");
        }

        if (!IZVORI.contains(izvor)) {
            throw new IllegalArgumentException("Izvor mora biti RUCNO, MENI, SLIKA ili OPIS.");
        }

        List<ObrokRequest.Stavka> stavke = request.getStavke();

        if (stavke == null || stavke.isEmpty()) {
            throw new IllegalArgumentException("Obrok mora imati barem jedno jelo.");
        }

        for (ObrokRequest.Stavka stavka : stavke) {

            if (stavka == null || stavka.getIdJelo() == null) {
                throw new IllegalArgumentException("Svaka stavka mora imati jelo.");
            }

            BigDecimal kolicina = stavka.getKolicina();

            if (kolicina == null || kolicina.signum() <= 0) {
                throw new IllegalArgumentException("Količina mora biti veća od 0.");
            }

            if (kolicina.multiply(BigDecimal.valueOf(2)).stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException("Količina mora biti višekratnik od 0,5.");
            }

            if (kolicina.compareTo(NAJVISE_PORCIJA) > 0) {
                throw new IllegalArgumentException("Količina može biti najviše 99,5 porcija.");
            }
        }

        Set<Long> idJela = stavke.stream().map(ObrokRequest.Stavka::getIdJelo).collect(Collectors.toSet());
        Map<Long, Jelo> jela = jeloRepository.findAllById(idJela).stream()
                .collect(Collectors.toMap(Jelo::getId, Function.identity()));

        for (Long idJelo : idJela) {
            Jelo jelo = jela.get(idJelo);

            if (jelo == null) {
                throw new IllegalArgumentException("Jelo " + idJelo + " ne postoji.");
            }

            if (!Boolean.TRUE.equals(jelo.getAktivno()) && !dopustenaNeaktivna.contains(idJelo)) {
                throw new IllegalArgumentException("Jelo " + idJelo + " nije aktivno.");
            }
        }
    }

    /** Stavke svih obroka dohvaćaju se jednim upitom; totali se zbrajaju iz istog rezultata. */
    private List<ObrokDTO> mapiraj(List<Obrok> obroci) {

        if (obroci.isEmpty()) {
            return List.of();
        }

        Map<Long, List<ObrokDTO.Stavka>> stavkePoObroku = konzumacijaRepository
                .findStavkeObroka(obroci.stream().map(Obrok::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(
                        StavkaObrokaRed::getIdObrok,
                        Collectors.mapping(this::uStavku, Collectors.toList())
                ));

        return obroci.stream()
                .map(o -> {
                    List<ObrokDTO.Stavka> stavke = stavkePoObroku.getOrDefault(o.getId(), List.of());

                    return new ObrokDTO(
                            o.getId(),
                            o.getDatum(),
                            o.getVrijeme().format(FORMAT_VREMENA),
                            o.getTipObroka(),
                            o.getIzvor(),
                            stavke,
                            zbroj(stavke, ObrokDTO.Stavka::getKalorije),
                            zbroj(stavke, ObrokDTO.Stavka::getProteini),
                            zbroj(stavke, ObrokDTO.Stavka::getUgljikohidrati),
                            zbroj(stavke, ObrokDTO.Stavka::getMasti)
                    );
                })
                .toList();
    }

    private ObrokDTO.Stavka uStavku(StavkaObrokaRed red) {
        BigDecimal kolicina = red.getKolicina();

        return new ObrokDTO.Stavka(
                red.getIdJelo(),
                red.getNaziv(),
                kolicina,
                puta(red.getKalorije(), kolicina),
                puta(red.getProteini(), kolicina),
                puta(red.getUgljikohidrati(), kolicina),
                puta(red.getMasti(), kolicina)
        );
    }

    private BigDecimal puta(BigDecimal vrijednost, BigDecimal kolicina) {
        return vrijednost.multiply(kolicina).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zbroj(List<ObrokDTO.Stavka> stavke, Function<ObrokDTO.Stavka, BigDecimal> vrijednost) {
        return stavke.stream().map(vrijednost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
