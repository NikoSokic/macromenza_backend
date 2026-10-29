package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.BrzoMjerenjeDTO;
import com.macromenza.macromenza.dto.BrzoMjerenjeRequest;
import com.macromenza.macromenza.dto.MjerenjeMaseDTO;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Brzo mjerenje (jedno po danu, preporuka se preračuna) i graf mase. Bez baze. */
class MjerenjeServiceTest {

    private static final long KORISNIK = 7L;
    private static final LocalDate DANAS = LocalDate.now(ObrokService.ZAGREB);

    private MjerenjeRepository mjerenjeRepository;
    private IzracunPreporukeService izracunPreporukeService;
    private MjerenjeService servis;

    @BeforeEach
    void postavi() {
        mjerenjeRepository = mock(MjerenjeRepository.class);
        izracunPreporukeService = mock(IzracunPreporukeService.class);
        when(mjerenjeRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        when(izracunPreporukeService.izracunajPreporuku(anyLong())).thenReturn(new Preporuka());
        servis = new MjerenjeService(mjerenjeRepository, izracunPreporukeService);
    }

    @Test
    void novoDanasnjeMjerenjePreuzimaAktivnostICilj() {
        Mjerenje zadnje = mjerenje(1L, DANAS.minusDays(3), "81.0");
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(KORISNIK)).thenReturn(List.of(zadnje));
        when(mjerenjeRepository.findFirstByIdKorisnikAndDatumOrderByIdDesc(KORISNIK, DANAS)).thenReturn(Optional.empty());

        BrzoMjerenjeDTO r = servis.brzoMjerenje(zahtjev("80.9"));

        assertThat(r.getMjerenje().getDatum()).isEqualTo(DANAS);
        assertThat(r.getMjerenje().getMasa()).isEqualByComparingTo("80.9");
        assertThat(r.getPreporuka()).isNotNull();
        verify(mjerenjeRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(m ->
                m.getIdKorisnik() == KORISNIK
                        && "umjerena".equals(m.getRazina_aktivnosti())
                        && "mrsavljenje".equals(m.getTip_cilja())));
        verify(izracunPreporukeService).izracunajPreporuku(KORISNIK);
    }

    @Test
    void postojeceDanasnjeMjerenjeSeAzurira() {
        Mjerenje danas = mjerenje(5L, DANAS, "80.0");
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(KORISNIK)).thenReturn(List.of(danas));
        when(mjerenjeRepository.findFirstByIdKorisnikAndDatumOrderByIdDesc(KORISNIK, DANAS)).thenReturn(Optional.of(danas));

        BrzoMjerenjeDTO r = servis.brzoMjerenje(zahtjev("80.1"));

        assertThat(r.getMjerenje().getId()).isEqualTo(5L);
        assertThat(danas.getMasa()).isEqualByComparingTo("80.1");
    }

    @Test
    void cijeliBrojDobivaJednuDecimalu() {
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(KORISNIK)).thenReturn(List.of(mjerenje(1L, DANAS, "80")));
        when(mjerenjeRepository.findFirstByIdKorisnikAndDatumOrderByIdDesc(eq(KORISNIK), any())).thenReturn(Optional.empty());

        assertThat(servis.brzoMjerenje(zahtjev("80")).getMjerenje().getMasa().toPlainString()).isEqualTo("80.0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"20", "20.0", "75.5", "400", "400.0"})
    void dopusteneMase(String masa) {
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(KORISNIK)).thenReturn(List.of(mjerenje(1L, DANAS, "80")));
        when(mjerenjeRepository.findFirstByIdKorisnikAndDatumOrderByIdDesc(eq(KORISNIK), any())).thenReturn(Optional.empty());

        assertThat(servis.brzoMjerenje(zahtjev(masa)).getMjerenje().getMasa()).isEqualByComparingTo(masa);
    }

    @ParameterizedTest
    @ValueSource(strings = {"19.9", "400.1", "0", "-80"})
    void masaIzvanRasponaSeOdbija(String masa) {
        assertThatThrownBy(() -> servis.brzoMjerenje(zahtjev(masa))).hasMessage("Masa mora biti između 20 i 400 kg.");
        verify(mjerenjeRepository, never()).saveAndFlush(any());
    }

    @Test
    void viseOdJedneDecimaleSeOdbija() {
        assertThatThrownBy(() -> servis.brzoMjerenje(zahtjev("80.05")))
                .hasMessage("Masa smije imati najviše jednu decimalu.");
    }

    @Test
    void bezMaseIKorisnikaIOnboardinga() {
        assertThatThrownBy(() -> servis.brzoMjerenje(zahtjev(null))).hasMessage("Masa mora biti između 20 i 400 kg.");

        BrzoMjerenjeRequest bezKorisnika = zahtjev("80");
        bezKorisnika.setIdKorisnik(null);
        assertThatThrownBy(() -> servis.brzoMjerenje(bezKorisnika)).hasMessage("idKorisnik je obavezan.");

        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(KORISNIK)).thenReturn(List.of());
        assertThatThrownBy(() -> servis.brzoMjerenje(zahtjev("80")))
                .hasMessage("Korisnik nema mjerenje (prvo završi onboarding).");
    }

    @Test
    void grafUzimaZadnjeMjerenjeDana() {
        LocalDate jucer = DANAS.minusDays(1);
        List<Mjerenje> mjerenja = List.of(
                mjerenje(1L, jucer.minusDays(1), "81.2"),
                mjerenje(2L, jucer, "81.0"),
                mjerenje(3L, jucer, "80.8"), // duplikat dana: vrijedi kasniji
                mjerenje(4L, DANAS, "80.6")
        );
        when(mjerenjeRepository.findByIdKorisnikAndDatumBetweenOrderByDatumAscIdAsc(eq(KORISNIK), any(), any()))
                .thenReturn(mjerenja);

        List<MjerenjeMaseDTO> mase = servis.dohvatiMase(KORISNIK, null, null);

        assertThat(mase).extracting(MjerenjeMaseDTO::getId).containsExactly(1L, 3L, 4L);
        assertThat(mase.get(1).getMasa()).isEqualByComparingTo("80.8");
        verify(mjerenjeRepository).findByIdKorisnikAndDatumBetweenOrderByDatumAscIdAsc(
                KORISNIK, LocalDate.of(1900, 1, 1), LocalDate.of(9999, 12, 31));
    }

    private BrzoMjerenjeRequest zahtjev(String masa) {
        BrzoMjerenjeRequest r = new BrzoMjerenjeRequest();
        r.setIdKorisnik(KORISNIK);
        r.setMasa(masa == null ? null : new BigDecimal(masa));
        return r;
    }

    private Mjerenje mjerenje(Long id, LocalDate datum, String masa) {
        Mjerenje m = new Mjerenje();
        ReflectionTestUtils.setField(m, "id", id);
        m.setIdKorisnik(KORISNIK);
        m.setDatum(datum);
        m.setMasa(new BigDecimal(masa));
        m.setRazina_aktivnosti("umjerena");
        m.setTip_cilja("mrsavljenje");
        return m;
    }
}
