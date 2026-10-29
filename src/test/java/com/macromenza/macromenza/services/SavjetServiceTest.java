package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.DanPregledDTO;
import com.macromenza.macromenza.dto.NutritivneVrijednostiDTO;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.JeloRepository.SavjetJeloRed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Pravila savjeta dana: proteini poslijepodne, lagana večer, opći savjet. Bez baze. */
class SavjetServiceTest {

    private static final LocalDate DANAS = LocalDate.of(2026, 10, 28);
    private static final NutritivneVrijednostiDTO CILJ = vrijednosti("2400", "160");

    private JeloRepository jeloRepository;
    private SavjetService servis;

    @BeforeEach
    void postavi() {
        jeloRepository = mock(JeloRepository.class);
        List<SavjetJeloRed> proteinska = List.of(red(11L, "Pileći file na žaru", "210", "38"));
        List<SavjetJeloRed> lagana = List.of(red(12L, "Zelena salata", "25", "1"));
        when(jeloRepository.findProteinskaJela(any(Limit.class))).thenReturn(proteinska);
        when(jeloRepository.findLaganaJela(anyCollection(), any(Limit.class))).thenReturn(lagana);
        servis = new SavjetService(jeloRepository);
    }

    @Test
    void premaloProteinaPoslijepodne() {
        DanPregledDTO.Savjet s = servis.odaberi(CILJ, vrijednosti("1200", "40"), DANAS, DANAS, LocalTime.of(16, 0));

        assertThat(s.getNaslov()).isEqualTo("Nedostaje ti proteina");
        assertThat(s.getJela()).extracting(DanPregledDTO.SavjetJelo::getNaziv).containsExactly("Pileći file na žaru");
    }

    @Test
    void pravilProteinaVrijediOd15h() {
        assertThat(servis.odaberi(CILJ, vrijednosti("900", "40"), DANAS, DANAS, LocalTime.of(15, 0)).getNaslov())
                .isEqualTo("Nedostaje ti proteina");
        assertThat(servis.odaberi(CILJ, vrijednosti("900", "40"), DANAS, DANAS, LocalTime.of(14, 59)).getNaslov())
                .isEqualTo("Savjet za menzu");
    }

    @Test
    void polaCiljaProteinaJeDovoljno() {
        assertThat(servis.odaberi(CILJ, vrijednosti("1500", "80"), DANAS, DANAS, LocalTime.of(16, 0)).getNaslov())
                .isEqualTo("Savjet za menzu");
    }

    @Test
    void navecerIznadCiljaPredlazeLaganaJela() {
        DanPregledDTO.Savjet s = servis.odaberi(CILJ, vrijednosti("2700", "120"), DANAS, DANAS, LocalTime.of(19, 30));

        assertThat(s.getNaslov()).isEqualTo("Laganija večera");
        assertThat(s.getJela()).extracting(DanPregledDTO.SavjetJelo::getNaziv).containsExactly("Zelena salata");
    }

    @Test
    void tocnoDesetPostoIznadCiljaNijePrekoracenje() {
        // 2400 · 1,1 = 2640
        assertThat(servis.odaberi(CILJ, vrijednosti("2640", "120"), DANAS, DANAS, LocalTime.of(20, 0)).getNaslov())
                .isEqualTo("Savjet za menzu");
    }

    @Test
    void proteiniImajuPrednostPredVecerom() {
        assertThat(servis.odaberi(CILJ, vrijednosti("2800", "40"), DANAS, DANAS, LocalTime.of(20, 0)).getNaslov())
                .isEqualTo("Nedostaje ti proteina");
    }

    @Test
    void prosliDanUvijekDobivaOpciSavjet() {
        DanPregledDTO.Savjet s = servis.odaberi(
                CILJ, vrijednosti("2800", "40"), DANAS.minusDays(1), DANAS, LocalTime.of(20, 0));

        assertThat(s.getNaslov()).isEqualTo("Savjet za menzu");
        assertThat(s.getJela()).isEmpty();
        verifyNoInteractions(jeloRepository);
    }

    @Test
    void bezCiljaOpciSavjetPoDanuUGodini() {
        DanPregledDTO.Savjet prvi = servis.odaberi(null, vrijednosti("0", "0"), DANAS, DANAS, LocalTime.of(20, 0));
        DanPregledDTO.Savjet isti = servis.odaberi(null, vrijednosti("500", "10"), DANAS, DANAS, LocalTime.of(8, 0));
        DanPregledDTO.Savjet sutra = servis.odaberi(
                null, vrijednosti("0", "0"), DANAS.plusDays(1), DANAS.plusDays(1), LocalTime.of(8, 0));

        assertThat(prvi.getTekst()).isNotBlank().isEqualTo(isti.getTekst());
        assertThat(sutra.getTekst()).isNotEqualTo(prvi.getTekst());
    }

    private static NutritivneVrijednostiDTO vrijednosti(String kcal, String proteini) {
        return new NutritivneVrijednostiDTO(new BigDecimal(kcal), new BigDecimal(proteini), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static SavjetJeloRed red(Long id, String naziv, String kcal, String proteini) {
        SavjetJeloRed r = mock(SavjetJeloRed.class);
        when(r.getIdJelo()).thenReturn(id);
        when(r.getNaziv()).thenReturn(naziv);
        when(r.getKalorije()).thenReturn(new BigDecimal(kcal));
        when(r.getProteini()).thenReturn(new BigDecimal(proteini));
        return r;
    }
}
