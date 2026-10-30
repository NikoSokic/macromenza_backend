package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.IzracunDTO;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.model.ProfilPodaci;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import com.macromenza.macromenza.repository.PreporukaRepository;
import com.macromenza.macromenza.repository.ProfilPodaciRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Mifflin-St Jeor × aktivnost + cilj; vrijednosti su ručno izračunate. Bez baze. */
class IzracunPreporukeServiceTest {

    private MjerenjeRepository mjerenjeRepository;
    private ProfilPodaciRepository profilRepository;
    private PreporukaRepository preporukaRepository;
    private IzracunPreporukeService servis;

    @BeforeEach
    void postavi() {
        mjerenjeRepository = mock(MjerenjeRepository.class);
        profilRepository = mock(ProfilPodaciRepository.class);
        preporukaRepository = mock(PreporukaRepository.class);
        when(preporukaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        servis = new IzracunPreporukeService(mjerenjeRepository, profilRepository, preporukaRepository);
    }

    @Test
    void muskiUmjerenaOdrzavanje() {
        // BMR = 10·80 + 6,25·180 − 5·25 + 5 = 1805; TDEE = 1805 · 1,55 = 2797,75
        IzracunDTO r = servis.izracunaj("M", 25, 180, 80, "umjerena", "odrzavanje");

        assertThat(r.getBmr()).isEqualTo(1805.0);
        assertThat(r.getBmrFormula()).isEqualTo("10 × 80 + 6,25 × 180 − 5 × 25 + 5");
        assertThat(r.getFaktorAktivnosti()).isEqualTo(1.55);
        assertThat(r.getTdee()).isEqualTo(2797.8);
        assertThat(r.getKorekcijaCilja()).isEqualTo(0.0);
        assertThat(r.getCiljKalorije()).isEqualTo(2798);
        assertThat(r.getProteini()).isEqualTo(160);      // 2 g/kg
        assertThat(r.getMasti()).isEqualTo(78);          // 2797,75 · 0,25 / 9 = 77,7
        assertThat(r.getUgljikohidrati()).isEqualTo(365); // (2797,75 − 640 − 699,44) / 4 = 364,6
    }

    @Test
    void zenskiSjedilackaMrsavljenje() {
        // BMR = 600 + 1031,25 − 150 − 161 = 1320,25; TDEE = 1584,3; −500 = 1084,3
        IzracunDTO r = servis.izracunaj("Z", 30, 165, 60, "sjedilacka", "mrsavljenje");

        assertThat(r.getBmr()).isEqualTo(1320.3);
        assertThat(r.getBmrFormula()).endsWith("− 161");
        assertThat(r.getTdee()).isEqualTo(1584.3);
        assertThat(r.getKorekcijaCilja()).isEqualTo(-500.0);
        assertThat(r.getCiljKalorije()).isEqualTo(1084);
        assertThat(r.getProteini()).isEqualTo(120);
        assertThat(r.getMasti()).isEqualTo(30);
        assertThat(r.getUgljikohidrati()).isEqualTo(83);
    }

    @ParameterizedTest
    @CsvSource({
            "sjedilacka, 1.2",
            "lagana, 1.375",
            "umjerena, 1.55",
            "visoka, 1.725",
            "VISOKA, 1.725",
            "nepoznata, 1.2"
    })
    void faktoriAktivnosti(String razina, double faktor) {
        IzracunDTO r = servis.izracunaj("M", 25, 180, 80, razina, "odrzavanje");

        assertThat(r.getFaktorAktivnosti()).isEqualTo(faktor);
        assertThat(r.getTdee()).isCloseTo(1805 * faktor, within(0.05));
    }

    @ParameterizedTest
    @CsvSource({
            "mrsavljenje, -500",
            "odrzavanje, 0",
            "dobivanje_mase, 300",
            "nepoznat, 0"
    })
    void korekcijaPremaCilju(String cilj, double korekcija) {
        IzracunDTO r = servis.izracunaj("M", 25, 180, 80, "visoka", cilj);

        assertThat(r.getKorekcijaCilja()).isEqualTo(korekcija);
        assertThat(r.getCiljKalorije()).isEqualTo((int) Math.round(1805 * 1.725 + korekcija));
    }

    @Test
    void spolNijeOsjetljivNaVelikaSlova() {
        assertThat(servis.izracunaj("m", 25, 180, 80, "umjerena", "odrzavanje").getBmr()).isEqualTo(1805.0);
    }

    @Test
    void makronutrijentiDajuCiljneKalorije() {
        IzracunDTO r = servis.izracunaj("Z", 22, 170, 63.5, "lagana", "dobivanje_mase");

        int izMakroa = r.getProteini() * 4 + r.getUgljikohidrati() * 4 + r.getMasti() * 9;
        // Svaki makro je zaokružen zasebno, pa odstupanje smije biti par kcal.
        assertThat(izMakroa).isCloseTo(r.getCiljKalorije(), within(10));
        assertThat(r.getBmrFormula()).startsWith("10 × 63,5 + ");
    }

    @Test
    void spremaPreporukuIzZadnjegMjerenjaIProfila() {
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(7L)).thenReturn(List.of(mjerenje(7L, "80.0")));
        when(profilRepository.findByIdKorisnik(7L)).thenReturn(Optional.of(profil(7L, "M", 25, 180)));

        Preporuka p = servis.izracunajPreporuku(7L);

        assertThat(p.getIdKorisnik()).isEqualTo(7L);
        assertThat(p.getKalorije()).isEqualTo(2798);
        assertThat(p.getProteini()).isEqualTo(160);
        assertThat(p.getMasti()).isEqualTo(78);
        assertThat(p.getUgljikohidrati()).isEqualTo(365);
        assertThat(p.getDatum()).isEqualTo(LocalDate.now());
    }

    @Test
    void bezMjerenjaBacaGresku() {
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(7L)).thenReturn(List.of());

        assertThatThrownBy(() -> servis.izracunajPreporuku(7L)).hasMessage("Korisnik nema mjerenje");
        verify(preporukaRepository, never()).save(any());
    }

    @Test
    void bezProfilaBacaGresku() {
        when(mjerenjeRepository.findZadnjaMjerenjaZaKorisnika(7L)).thenReturn(List.of(mjerenje(7L, "80.0")));
        when(profilRepository.findByIdKorisnik(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servis.izracunajPreporuku(7L)).hasMessage("Korisnik nema profil podatke");
    }

    private Mjerenje mjerenje(Long idKorisnik, String masa) {
        Mjerenje m = new Mjerenje();
        m.setIdKorisnik(idKorisnik);
        m.setMasa(new BigDecimal(masa));
        m.setRazina_aktivnosti("umjerena");
        m.setTip_cilja("odrzavanje");
        m.setDatum(LocalDate.now());
        return m;
    }

    private ProfilPodaci profil(Long idKorisnik, String spol, int dob, int visina) {
        ProfilPodaci p = new ProfilPodaci();
        p.setIdKorisnik(idKorisnik);
        p.setSpol(spol);
        p.setDob(dob);
        p.setVisina(visina);
        return p;
    }
}
