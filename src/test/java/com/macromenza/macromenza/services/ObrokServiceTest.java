package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.DanPregledDTO;
import com.macromenza.macromenza.dto.ObrokDTO;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.dto.TjedanDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Obrok;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository.StavkaObrokaRed;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.repository.ObrokRepository.TipObrokaRed;
import com.macromenza.macromenza.repository.ObrokRepository.TjedanDanRed;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Spremanje, uređivanje, brisanje, pregled dana i tjedna. Repozitoriji su lažni, bez baze. */
class ObrokServiceTest {

    private static final long KORISNIK = 7L;
    private static final LocalDate DANAS = LocalDate.now(ObrokService.ZAGREB);

    private ObrokRepository obrokRepository;
    private KonzumacijaRepository konzumacijaRepository;
    private JeloRepository jeloRepository;
    private KorisnikRepository korisnikRepository;
    private PreporukaRepository preporukaRepository;
    private SavjetService savjetService;
    private ObrokService servis;

    @BeforeEach
    void postavi() {
        obrokRepository = mock(ObrokRepository.class);
        konzumacijaRepository = mock(KonzumacijaRepository.class);
        jeloRepository = mock(JeloRepository.class);
        korisnikRepository = mock(KorisnikRepository.class);
        preporukaRepository = mock(PreporukaRepository.class);
        savjetService = mock(SavjetService.class);

        // 1 = Grah varivo (aktivno), 2 = Kruh (aktivno), 3 = Staro jelo (neaktivno)
        List<Jelo> jela = List.of(jelo(1L, true), jelo(2L, true), jelo(3L, false));
        when(jeloRepository.findAllById(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return jela.stream().filter(j -> ids.contains(j.getId())).toList();
        });

        when(korisnikRepository.existsById(KORISNIK)).thenReturn(true);
        when(obrokRepository.save(any())).thenAnswer(inv -> {
            Obrok o = inv.getArgument(0);
            if (o.getId() == null) {
                ReflectionTestUtils.setField(o, "id", 100L);
            }
            return o;
        });
        List<StavkaObrokaRed> stavke = List.of(
                stavka(100L, 1L, "Grah varivo", "1.5", "320.40", "18.20", "40.10", "9.55"),
                stavka(100L, 2L, "Kruh", "1.0", "70.00", "2.40", "14.00", "0.60")
        );
        when(konzumacijaRepository.findStavkeObroka(any())).thenReturn(stavke);

        servis = new ObrokService(obrokRepository, konzumacijaRepository, jeloRepository,
                korisnikRepository, preporukaRepository, savjetService);
    }

    // ---------- spremanje ----------

    @Test
    void spremaObrokISveStavkeUJednomPozivu() {
        ObrokDTO o = servis.spremiObrok(zahtjev(stavka(1L, "1.5"), stavka(2L, "1")));

        ArgumentCaptor<Obrok> obrok = ArgumentCaptor.forClass(Obrok.class);
        verify(obrokRepository).save(obrok.capture());
        assertThat(obrok.getValue().getIzvor()).isEqualTo("RUCNO");
        assertThat(obrok.getValue().getVrijeme()).isEqualTo(LocalTime.of(12, 30)); // sekunde odrezane

        List<Konzumacija> konzumacije = spremljeneKonzumacije();
        assertThat(konzumacije).hasSize(2);
        assertThat(konzumacije).allSatisfy(k -> {
            assertThat(k.getIdObrok()).isEqualTo(100L);
            assertThat(k.getIdKorisnik()).isEqualTo(KORISNIK);
            assertThat(k.getDatum()).isEqualTo(DANAS);
            assertThat(k.getTip_obroka()).isEqualTo("RUCAK");
        });
        assertThat(konzumacije.get(0).getKolicina()).isEqualByComparingTo("1.5");

        // Stavke su (vrijednost porcije × količina), totali zbroj stavki.
        assertThat(o.getId()).isEqualTo(100L);
        assertThat(o.getVrijeme()).isEqualTo("12:30");
        assertThat(o.getStavke()).hasSize(2);
        assertThat(o.getStavke().get(0).getKalorije()).isEqualByComparingTo("480.60");
        assertThat(o.getStavke().get(0).getMasti()).isEqualByComparingTo("14.33"); // 14,325 → HALF_UP
        assertThat(o.getUkupnoKalorije()).isEqualByComparingTo("550.60");
        assertThat(o.getUkupnoProteini()).isEqualByComparingTo("29.70");
        assertThat(o.getUkupnoUgljikohidrati()).isEqualByComparingTo("74.15");
        assertThat(o.getUkupnoMasti()).isEqualByComparingTo("14.93");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "1", "2.5", "99.5"})
    void dopusteneKolicine(String kolicina) {
        servis.spremiObrok(zahtjev(stavka(1L, kolicina)));
        assertThat(spremljeneKonzumacije().get(0).getKolicina()).isEqualByComparingTo(kolicina);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.3", "1.25", "100"})
    void nedopusteneKolicine(String kolicina) {
        assertThatThrownBy(() -> servis.spremiObrok(zahtjev(stavka(1L, kolicina))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Količina");
        verify(obrokRepository, never()).save(any());
    }

    @Test
    void svaCetiriTipaObrokaIIzvoriSuDopusteni() {
        for (String tip : List.of("DORUCAK", "RUCAK", "UZINA", "VECERA")) {
            for (String izvor : List.of("RUCNO", "MENI", "SLIKA", "OPIS", "PRIMJER")) {
                ObrokRequest r = zahtjev(stavka(1L, "1"));
                r.setTipObroka(tip);
                r.setIzvor(izvor);
                servis.spremiObrok(r);
            }
        }
    }

    @Test
    void neispravniZahtjeviSeOdbijaju() {
        odbija(r -> r.setIdKorisnik(null), "Korisnik je obavezan.");
        odbija(r -> r.setIdKorisnik(999L), "Korisnik ne postoji.");
        odbija(r -> r.setDatum(null), "Datum je obavezan.");
        odbija(r -> r.setDatum(DANAS.plusDays(1)), "Datum obroka ne može biti u budućnosti.");
        odbija(r -> r.setVrijeme(null), "Vrijeme je obavezno.");
        odbija(r -> r.setTipObroka("MARENDA"), "Tip obroka mora biti DORUCAK, RUCAK, UZINA ili VECERA.");
        odbija(r -> r.setTipObroka(null), "Tip obroka mora biti DORUCAK, RUCAK, UZINA ili VECERA.");
        odbija(r -> r.setIzvor("GLAS"), "Izvor mora biti RUCNO, MENI, SLIKA, OPIS ili PRIMJER.");
        odbija(r -> r.setStavke(List.of()), "Obrok mora imati barem jedno jelo.");
        odbija(r -> r.setStavke(null), "Obrok mora imati barem jedno jelo.");
        odbija(r -> r.setStavke(List.of(stavka(null, "1"))), "Svaka stavka mora imati jelo.");
        odbija(r -> r.setStavke(List.of(stavka(1L, null))), "Količina mora biti veća od 0.");
        odbija(r -> r.setStavke(List.of(stavka(42L, "1"))), "Jelo 42 ne postoji.");
        odbija(r -> r.setStavke(List.of(stavka(3L, "1"))), "Jelo 3 nije aktivno.");
        verify(obrokRepository, never()).save(any());
    }

    // ---------- uređivanje i brisanje ----------

    @Test
    void uredivanjeZamjenjujeStavkeIZadrzavaVlasnika() {
        Obrok postojeci = obrok(100L);
        when(obrokRepository.findByIdAndIdKorisnik(100L, KORISNIK)).thenReturn(Optional.of(postojeci));
        List<Konzumacija> stare = List.of(konzumacija(3L));
        when(konzumacijaRepository.findByIdObrokOrderByIdAsc(100L)).thenReturn(stare);

        // Neaktivno jelo 3 već je u obroku pa smije ostati; idKorisnik iz zahtjeva se zanemaruje.
        ObrokRequest r = zahtjev(stavka(3L, "2"), stavka(2L, "0.5"));
        r.setIdKorisnik(555L);
        r.setTipObroka("VECERA");
        servis.azurirajObrok(100L, KORISNIK, r);

        verify(konzumacijaRepository).deleteAllInBatch(stare);
        List<Konzumacija> nove = spremljeneKonzumacije();
        assertThat(nove).extracting(Konzumacija::getIdJelo).containsExactly(3L, 2L);
        assertThat(nove).allSatisfy(k -> {
            assertThat(k.getIdKorisnik()).isEqualTo(KORISNIK);
            assertThat(k.getTip_obroka()).isEqualTo("VECERA");
        });
        assertThat(postojeci.getTipObroka()).isEqualTo("VECERA");
    }

    @Test
    void uredivanjeNeDopustaNovoNeaktivnoJelo() {
        when(obrokRepository.findByIdAndIdKorisnik(100L, KORISNIK)).thenReturn(Optional.of(obrok(100L)));
        when(konzumacijaRepository.findByIdObrokOrderByIdAsc(100L)).thenReturn(List.of(konzumacija(1L)));

        assertThatThrownBy(() -> servis.azurirajObrok(100L, KORISNIK, zahtjev(stavka(3L, "1"))))
                .hasMessage("Jelo 3 nije aktivno.");
        verify(konzumacijaRepository, never()).deleteAllInBatch(any());
    }

    @Test
    void tudiObrokSeNeMozeNiUreditiNiObrisati() {
        when(obrokRepository.findByIdAndIdKorisnik(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servis.azurirajObrok(100L, KORISNIK, zahtjev(stavka(1L, "1"))))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> servis.obrisiObrok(100L, KORISNIK))
                .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> servis.dohvatiObrok(100L, KORISNIK))
                .isInstanceOf(NoSuchElementException.class);
        verify(obrokRepository, never()).delete(any());
    }

    @Test
    void brisanjeBriseObrok() {
        Obrok o = obrok(100L);
        when(obrokRepository.findByIdAndIdKorisnik(100L, KORISNIK)).thenReturn(Optional.of(o));

        servis.obrisiObrok(100L, KORISNIK);

        verify(obrokRepository).delete(o);
    }

    @Test
    void danBezObrokaJePrazanPopis() {
        when(obrokRepository.findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(KORISNIK, DANAS)).thenReturn(List.of());
        assertThat(servis.dohvatiObroke(KORISNIK, DANAS)).isEmpty();
    }

    // ---------- pregled dana ----------

    @Test
    void danPregledZbrajaTipoveUFiksnomRedoslijedu() {
        List<TipObrokaRed> tipovi = List.of(
                tip("VECERA", 1, "600.004", "30", "70", "20"),
                tip("DORUCAK", 2, "450.555", "20.005", "50", "15")
        );
        when(obrokRepository.findTotaliPoTipu(KORISNIK, DANAS)).thenReturn(tipovi);
        when(preporukaRepository.findTopByIdKorisnikAndDatumLessThanEqualOrderByDatumDescIdDesc(KORISNIK, DANAS))
                .thenReturn(Optional.of(preporuka(2400, 160, 290, 67)));

        DanPregledDTO d = servis.dohvatiDanPregled(KORISNIK, null);

        assertThat(d.getPoTipu()).extracting(DanPregledDTO.PoTipu::getTipObroka)
                .containsExactly("DORUCAK", "RUCAK", "UZINA", "VECERA");
        assertThat(d.getPoTipu()).extracting(DanPregledDTO.PoTipu::getBrojObroka).containsExactly(2L, 0L, 0L, 1L);
        assertThat(d.getPoTipu().get(0).getKalorije()).isEqualByComparingTo("450.56");
        // Zbroj zaokruženih tipova = pojedeno.
        assertThat(d.getPojedeno().getKalorije()).isEqualByComparingTo("1050.56");
        assertThat(d.getPojedeno().getProteini()).isEqualByComparingTo("50.01");
        assertThat(d.getCilj().getKalorije()).isEqualByComparingTo("2400");
        assertThat(d.getCilj().getMasti()).isEqualByComparingTo("67");
    }

    @Test
    void danPregledBezCiljaINepostojeciKorisnik() {
        when(obrokRepository.findTotaliPoTipu(KORISNIK, DANAS)).thenReturn(List.of());
        when(preporukaRepository.findTopByIdKorisnikAndDatumLessThanEqualOrderByDatumDescIdDesc(KORISNIK, DANAS))
                .thenReturn(Optional.empty());

        DanPregledDTO d = servis.dohvatiDanPregled(KORISNIK, DANAS);
        assertThat(d.getCilj()).isNull();
        assertThat(d.getPojedeno().getKalorije()).isEqualByComparingTo("0");

        assertThatThrownBy(() -> servis.dohvatiDanPregled(999L, DANAS)).isInstanceOf(NoSuchElementException.class);
    }

    // ---------- tjedan ----------

    @Test
    void tjedanRacunaDaneUCiljuIProsjeke() {
        LocalDate pon = DANAS.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(2);
        List<TjedanDanRed> redovi = List.of(
                dan(pon, "2000", 2000, true),            // točno u cilju
                dan(pon.plusDays(1), "2200", 2000, false), // +10 % → još u cilju
                dan(pon.plusDays(2), "2201", 2000, false), // malo preko 10 %
                dan(pon.plusDays(3), null, 2000, false),   // bez unosa
                dan(pon.plusDays(4), "1500", null, false), // bez cilja
                dan(pon.plusDays(5), "1799", 2000, false), // −10,05 %
                dan(pon.plusDays(6), null, null, false)
        );
        when(obrokRepository.findTjedan(KORISNIK, pon)).thenReturn(redovi);

        // Bilo koji dan u tjednu daje isti tjedan (pon–ned).
        TjedanDTO t = servis.dohvatiTjedan(KORISNIK, pon.plusDays(3));

        assertThat(t.getPocetak()).isEqualTo(pon);
        assertThat(t.getKraj()).isEqualTo(pon.plusDays(6));
        assertThat(t.getDani()).extracting(TjedanDTO.Dan::getUCilju)
                .containsExactly(true, true, false, false, false, false, false);
        assertThat(t.getDani()).extracting(TjedanDTO.Dan::getImaUnos)
                .containsExactly(true, true, true, false, true, true, false);
        assertThat(t.getDaniSUnosom()).isEqualTo(5);
        assertThat(t.getDaniUCilju()).isEqualTo(2);
        // (2000 + 2200 + 2201 + 1500 + 1799) / 5
        assertThat(t.getProsjek().getKalorije()).isEqualByComparingTo("1940.00");
        assertThat(t.getProsjekCilj().getKalorije()).isEqualByComparingTo("2000.00");
        assertThat(t.getImaPrethodni()).isTrue();
        assertThat(t.getImaSljedeci()).isTrue();
    }

    @Test
    void prazanTjedanNemaProsjeka() {
        LocalDate pon = DANAS.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<TjedanDanRed> redovi = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            redovi.add(dan(pon.plusDays(i), null, null, false));
        }
        when(obrokRepository.findTjedan(KORISNIK, pon)).thenReturn(redovi);

        TjedanDTO t = servis.dohvatiTjedan(KORISNIK, null);

        assertThat(t.getProsjek()).isNull();
        assertThat(t.getProsjekCilj()).isNull();
        assertThat(t.getDaniSUnosom()).isZero();
        assertThat(t.getImaSljedeci()).isFalse();
    }

    @Test
    void buduciTjedanSeOdbija() {
        assertThatThrownBy(() -> servis.dohvatiTjedan(KORISNIK, DANAS.plusWeeks(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servis.dohvatiTjedan(999L, DANAS)).isInstanceOf(NoSuchElementException.class);
    }

    // ---------- pomoćno ----------

    private void odbija(java.util.function.Consumer<ObrokRequest> izmjena, String poruka) {
        ObrokRequest r = zahtjev(stavka(1L, "1"));
        izmjena.accept(r);
        assertThatThrownBy(() -> servis.spremiObrok(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(poruka);
    }

    @SuppressWarnings("unchecked")
    private List<Konzumacija> spremljeneKonzumacije() {
        ArgumentCaptor<List<Konzumacija>> c = ArgumentCaptor.forClass(List.class);
        verify(konzumacijaRepository, org.mockito.Mockito.atLeastOnce()).saveAllAndFlush(c.capture());
        return c.getValue();
    }

    private ObrokRequest zahtjev(ObrokRequest.Stavka... stavke) {
        ObrokRequest r = new ObrokRequest();
        r.setIdKorisnik(KORISNIK);
        r.setDatum(DANAS);
        r.setVrijeme(LocalTime.of(12, 30, 45));
        r.setTipObroka("RUCAK");
        r.setStavke(new ArrayList<>(List.of(stavke)));
        return r;
    }

    private ObrokRequest.Stavka stavka(Long idJelo, String kolicina) {
        return new ObrokRequest.Stavka(idJelo, kolicina == null ? null : new BigDecimal(kolicina));
    }

    private Obrok obrok(Long id) {
        Obrok o = new Obrok();
        ReflectionTestUtils.setField(o, "id", id);
        o.setIdKorisnik(KORISNIK);
        o.setDatum(DANAS);
        o.setVrijeme(LocalTime.of(12, 0));
        o.setTipObroka("RUCAK");
        o.setIzvor("RUCNO");
        return o;
    }

    private Konzumacija konzumacija(Long idJelo) {
        Konzumacija k = new Konzumacija();
        k.setIdJelo(idJelo);
        return k;
    }

    private Jelo jelo(Long id, boolean aktivno) {
        Jelo j = mock(Jelo.class);
        when(j.getId()).thenReturn(id);
        when(j.getAktivno()).thenReturn(aktivno);
        return j;
    }

    private Preporuka preporuka(int kcal, int p, int uh, int m) {
        Preporuka r = new Preporuka();
        r.setKalorije(kcal);
        r.setProteini(p);
        r.setUgljikohidrati(uh);
        r.setMasti(m);
        return r;
    }

    private StavkaObrokaRed stavka(Long idObrok, Long idJelo, String naziv, String kolicina,
                                   String kcal, String p, String uh, String m) {
        StavkaObrokaRed r = mock(StavkaObrokaRed.class);
        when(r.getIdObrok()).thenReturn(idObrok);
        when(r.getIdJelo()).thenReturn(idJelo);
        when(r.getNaziv()).thenReturn(naziv);
        when(r.getKolicina()).thenReturn(new BigDecimal(kolicina));
        when(r.getKalorije()).thenReturn(new BigDecimal(kcal));
        when(r.getProteini()).thenReturn(new BigDecimal(p));
        when(r.getUgljikohidrati()).thenReturn(new BigDecimal(uh));
        when(r.getMasti()).thenReturn(new BigDecimal(m));
        return r;
    }

    private TipObrokaRed tip(String tip, long broj, String kcal, String p, String uh, String m) {
        TipObrokaRed r = mock(TipObrokaRed.class);
        when(r.getTipObroka()).thenReturn(tip);
        when(r.getBrojObroka()).thenReturn(broj);
        when(r.getKalorije()).thenReturn(new BigDecimal(kcal));
        when(r.getProteini()).thenReturn(new BigDecimal(p));
        when(r.getUgljikohidrati()).thenReturn(new BigDecimal(uh));
        when(r.getMasti()).thenReturn(new BigDecimal(m));
        return r;
    }

    private TjedanDanRed dan(LocalDate datum, String kcal, Integer cilj, boolean prvi) {
        TjedanDanRed r = mock(TjedanDanRed.class);
        BigDecimal k = kcal == null ? null : new BigDecimal(kcal);
        when(r.getDatum()).thenReturn(datum);
        when(r.getKalorije()).thenReturn(k);
        when(r.getProteini()).thenReturn(k == null ? null : new BigDecimal("100"));
        when(r.getUgljikohidrati()).thenReturn(k == null ? null : new BigDecimal("200"));
        when(r.getMasti()).thenReturn(k == null ? null : new BigDecimal("60"));
        when(r.getCiljKalorije()).thenReturn(cilj);
        when(r.getCiljProteini()).thenReturn(cilj == null ? null : 160);
        when(r.getCiljUgljikohidrati()).thenReturn(cilj == null ? null : 250);
        when(r.getCiljMasti()).thenReturn(cilj == null ? null : 60);
        when(r.getImaPrethodni()).thenReturn(prvi);
        when(r.getImaPrimjer()).thenReturn(false);
        return r;
    }
}
