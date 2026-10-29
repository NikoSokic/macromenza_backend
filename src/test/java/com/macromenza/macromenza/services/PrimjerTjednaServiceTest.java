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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Primjer tjedna: slobodni dani, izbor najbližeg cilju, svaki meni jednom, izvor PRIMJER. Bez baze. */
class PrimjerTjednaServiceTest {

    private static final long KORISNIK = 7L;
    private static final LocalDate DANAS = LocalDate.now(ObrokService.ZAGREB);

    private ObrokRepository obrokRepository;
    private KonzumacijaRepository konzumacijaRepository;
    private KorisnikRepository korisnikRepository;
    private PreporukaRepository preporukaRepository;
    private JeloRepository jeloRepository;
    private JeloNutritivnoRepository nutritivnoRepository;
    private MeniService meniService;
    private PrimjerTjednaService servis;

    @BeforeEach
    void postavi() {
        obrokRepository = mock(ObrokRepository.class);
        konzumacijaRepository = mock(KonzumacijaRepository.class);
        korisnikRepository = mock(KorisnikRepository.class);
        preporukaRepository = mock(PreporukaRepository.class);
        jeloRepository = mock(JeloRepository.class);
        nutritivnoRepository = mock(JeloNutritivnoRepository.class);
        meniService = mock(MeniService.class);

        when(korisnikRepository.existsById(KORISNIK)).thenReturn(true);
        Preporuka cilj = new Preporuka();
        cilj.setKalorije(2000);
        when(preporukaRepository.findTopByIdKorisnikOrderByIdDesc(KORISNIK)).thenReturn(Optional.of(cilj));

        // Doručak: 10 Jogurt (300 kcal), 11 Kroasan (400 kcal); 12 je druga kategorija.
        List<Jelo> jela = List.of(
                jelo(11L, "Brza jela i doručak"), jelo(12L, "Variva"), jelo(10L, "Brza jela i doručak"));
        when(jeloRepository.findByAktivnoTrueOrderByNaziv()).thenReturn(jela);
        List<JeloNutritivno> kcal = List.of(nutritivno(10L, "300"), nutritivno(11L, "400"));
        when(nutritivnoRepository.findAllById(any())).thenReturn(kcal);

        // Ručkovi: 1 (800), 2 (900); 3 ima četvrt porcije, 4 nema vrijednosti → oba se preskaču.
        List<MeniDTO> rucci = List.of(
                meni(2L, "900", stavka(102L, "1.5")),
                meni(1L, "800", stavka(101L, "1")),
                meni(3L, "500", stavka(103L, "0.25")),
                meni(4L, null, stavka(104L, "1")));
        List<MeniDTO> vecere = List.of(meni(5L, "700", stavka(105L, "1")), meni(6L, "600", stavka(106L, "0.5")));
        when(meniService.dohvatiMenije("RUCAK", null)).thenReturn(rucci);
        when(meniService.dohvatiMenije("VECERA", null)).thenReturn(vecere);

        when(obrokRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        servis = new PrimjerTjednaService(obrokRepository, konzumacijaRepository, korisnikRepository,
                preporukaRepository, jeloRepository, nutritivnoRepository, meniService);
    }

    @Test
    void puniSlobodneDaneDokImaNeiskoristenihMenija() {
        // Prvi od 7 dana je zauzet; dva upotrebljiva ručka i dvije večere dosta su za dva dana.
        when(obrokRepository.existsByIdKorisnikAndDatum(KORISNIK, DANAS.minusDays(7))).thenReturn(true);

        Map<String, Integer> r = servis.ucitaj(KORISNIK);

        assertThat(r).containsExactly(
                Map.entry(DANAS.minusDays(6).toString(), 3),
                Map.entry(DANAS.minusDays(5).toString(), 3));

        ArgumentCaptor<Obrok> obroci = ArgumentCaptor.forClass(Obrok.class);
        verify(obrokRepository, times(6)).save(obroci.capture());
        assertThat(obroci.getAllValues()).allSatisfy(o -> {
            assertThat(o.getIzvor()).isEqualTo("PRIMJER");
            assertThat(o.getIdKorisnik()).isEqualTo(KORISNIK);
            assertThat(o.getDatum()).isBefore(DANAS);
        });
        assertThat(obroci.getAllValues()).extracting(Obrok::getTipObroka)
                .containsExactly("DORUCAK", "RUCAK", "VECERA", "DORUCAK", "RUCAK", "VECERA");

        // Dan 1: jogurt + meni 2 + meni 5 = 1900 (najbliže 2000). Dan 2: jogurt i kroasan + meni 1 + meni 6 = 2100.
        assertThat(jelaUObrocima()).containsExactly(
                List.of(10L), List.of(102L), List.of(105L),
                List.of(10L, 11L), List.of(101L), List.of(106L));
    }

    @Test
    void kolicineMenijaSePrenose() {
        servis.ucitaj(KORISNIK);

        assertThat(konzumacije().stream().flatMap(List::stream))
                .filteredOn(k -> k.getIdJelo() == 102L)
                .singleElement()
                .satisfies(k -> assertThat(k.getKolicina()).isEqualByComparingTo("1.5"));
    }

    @Test
    void sviDaniZauzetiNistaSeNeStvara() {
        when(obrokRepository.existsByIdKorisnikAndDatum(eq(KORISNIK), any())).thenReturn(true);

        assertThat(servis.ucitaj(KORISNIK)).isEmpty();
        verify(obrokRepository, never()).save(any());
    }

    @Test
    void danasSeNikadNeDira() {
        servis.ucitaj(KORISNIK);
        verify(obrokRepository, never()).existsByIdKorisnikAndDatum(KORISNIK, DANAS);
    }

    @Test
    void bezCiljaIliKorisnika() {
        when(preporukaRepository.findTopByIdKorisnikOrderByIdDesc(KORISNIK)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servis.ucitaj(KORISNIK)).hasMessage("Korisnik nema dnevni cilj (preporuku).");

        assertThatThrownBy(() -> servis.ucitaj(999L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void uklanjanjeBriseSamoPrimjer() {
        when(obrokRepository.obrisiPoIzvoru(KORISNIK, "PRIMJER")).thenReturn(21);
        assertThat(servis.ukloni(KORISNIK)).isEqualTo(21);
    }

    @SuppressWarnings("unchecked")
    private List<List<Konzumacija>> konzumacije() {
        ArgumentCaptor<List<Konzumacija>> c = ArgumentCaptor.forClass(List.class);
        verify(konzumacijaRepository, org.mockito.Mockito.atLeastOnce()).saveAllAndFlush(c.capture());
        return c.getAllValues();
    }

    private List<List<Long>> jelaUObrocima() {
        return konzumacije().stream().map(l -> l.stream().map(Konzumacija::getIdJelo).toList()).toList();
    }

    private Jelo jelo(Long id, String kategorija) {
        Jelo j = mock(Jelo.class);
        when(j.getId()).thenReturn(id);
        when(j.getKategorija()).thenReturn(kategorija);
        return j;
    }

    private JeloNutritivno nutritivno(Long id, String kcal) {
        JeloNutritivno n = mock(JeloNutritivno.class);
        when(n.getIdJelo()).thenReturn(id);
        when(n.getKalorije()).thenReturn(new BigDecimal(kcal));
        return n;
    }

    private MeniDTO meni(Long id, String kcal, MeniStavkaDTO... stavke) {
        return new MeniDTO(id, "M" + id, "RUCAK", false, id.intValue(),
                kcal == null ? null : new BigDecimal(kcal), null, null, null, List.of(stavke));
    }

    private MeniStavkaDTO stavka(Long idJelo, String kolicina) {
        return new MeniStavkaDTO(idJelo, "a", "Jelo " + idJelo, new BigDecimal(kolicina), false,
                null, null, null, null);
    }
}
