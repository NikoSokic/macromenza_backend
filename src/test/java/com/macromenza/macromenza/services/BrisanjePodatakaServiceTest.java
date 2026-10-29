package com.macromenza.macromenza.services;

import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import com.macromenza.macromenza.repository.ObrokRepository;
import com.macromenza.macromenza.repository.PreporukaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Brisanje svih podataka: traži potvrdu, račun i profil ostaju. Bez baze. */
class BrisanjePodatakaServiceTest {

    private KorisnikRepository korisnikRepository;
    private ObrokRepository obrokRepository;
    private KonzumacijaRepository konzumacijaRepository;
    private MjerenjeRepository mjerenjeRepository;
    private PreporukaRepository preporukaRepository;
    private BrisanjePodatakaService servis;

    @BeforeEach
    void postavi() {
        korisnikRepository = mock(KorisnikRepository.class);
        obrokRepository = mock(ObrokRepository.class);
        konzumacijaRepository = mock(KonzumacijaRepository.class);
        mjerenjeRepository = mock(MjerenjeRepository.class);
        preporukaRepository = mock(PreporukaRepository.class);
        servis = new BrisanjePodatakaService(korisnikRepository, obrokRepository, konzumacijaRepository,
                mjerenjeRepository, preporukaRepository);
    }

    @Test
    void brojiObrisaneRedovePoTablici() {
        when(korisnikRepository.existsById(7L)).thenReturn(true);
        when(konzumacijaRepository.countByIdKorisnik(7L)).thenReturn(12L);
        when(obrokRepository.obrisiSveZaKorisnika(7L)).thenReturn(5);
        when(mjerenjeRepository.obrisiSveZaKorisnika(7L)).thenReturn(3);
        when(preporukaRepository.obrisiSveZaKorisnika(7L)).thenReturn(2);

        Map<String, Long> r = servis.obrisiSvePodatke(7L, "OBRISI");

        assertThat(r).containsExactly(
                Map.entry("obrok", 5L), Map.entry("konzumacija", 12L),
                Map.entry("mjerenje", 3L), Map.entry("preporuka", 2L));
        verify(korisnikRepository, never()).deleteById(anyLong());
    }

    @Test
    void bezIspravnePotvrdeNistaSeNeBrise() {
        for (String potvrda : new String[]{null, "", "obrisi", "DA"}) {
            assertThatThrownBy(() -> servis.obrisiSvePodatke(7L, potvrda)).isInstanceOf(IllegalArgumentException.class);
        }
        verify(obrokRepository, never()).obrisiSveZaKorisnika(anyLong());
        verify(mjerenjeRepository, never()).obrisiSveZaKorisnika(anyLong());
    }

    @Test
    void nepostojeciKorisnik() {
        assertThatThrownBy(() -> servis.obrisiSvePodatke(999L, "OBRISI")).isInstanceOf(NoSuchElementException.class);
        verify(obrokRepository, never()).obrisiSveZaKorisnika(anyLong());
    }
}
