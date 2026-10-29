package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.JeloDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.JeloSastojakNutritivnoRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Limit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pretraga, kategorija i filter po makronutrijentima te "Često jedeš". Bez baze. */
class JeloServiceTest {

    private JeloRepository jeloRepository;
    private JeloNutritivnoRepository nutritivnoRepository;
    private KonzumacijaRepository konzumacijaRepository;
    private JeloService servis;

    @BeforeEach
    void postavi() {
        jeloRepository = mock(JeloRepository.class);
        nutritivnoRepository = mock(JeloNutritivnoRepository.class);
        konzumacijaRepository = mock(KonzumacijaRepository.class);

        // 1 Grah varivo 320 kcal / 18 g P, 2 Pohana piletina 450 / 30, 3 Kruh bez vrijednosti
        List<Jelo> jela = List.of(jelo(1L, "Grah varivo", "Variva"), jelo(2L, "Pohana piletina", "Meso"),
                jelo(3L, "Kruh", "Kruh"));
        List<JeloNutritivno> vrijednosti = List.of(nutritivno(1L, "320", "18"), nutritivno(2L, "450", "30"));
        when(jeloRepository.findByAktivnoTrueOrderByNaziv()).thenReturn(jela);
        when(jeloRepository.findByAktivnoTrueAndNazivContainingIgnoreCaseOrderByNaziv("grah"))
                .thenReturn(List.of(jela.get(0)));
        when(nutritivnoRepository.findAllById(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return vrijednosti.stream().filter(n -> ids.contains(n.getIdJelo())).toList();
        });

        servis = new JeloService(jeloRepository, nutritivnoRepository, konzumacijaRepository,
                mock(JeloSastojakNutritivnoRepository.class));
    }

    @Test
    void bezFilteraSvaAktivnaJela() {
        assertThat(id(servis.dohvatiJela(null))).containsExactly(1L, 2L, 3L);
        assertThat(id(servis.dohvatiJela("   "))).containsExactly(1L, 2L, 3L);
        verify(jeloRepository, never()).findByAktivnoTrueAndNazivContainingIgnoreCaseOrderByNaziv(any());
    }

    @Test
    void pretragaJeObrezana() {
        assertThat(id(servis.dohvatiJela("  grah "))).containsExactly(1L);
    }

    @Test
    void kategorijaNeovisnaOVeliciniSlova() {
        assertThat(id(servis.dohvatiJela(null, " variva ", null, null, null, null, null, null, null, null)))
                .containsExactly(1L);
    }

    @Test
    void rasponiMakronutrijenataSeKombiniraju() {
        assertThat(id(servis.filtrirajJela(new BigDecimal("300"), null, null, null, null, null, null, null)))
                .containsExactly(1L, 2L); // jelo bez vrijednosti ispada čim postoji filter
        assertThat(id(servis.filtrirajJela(null, new BigDecimal("400"), new BigDecimal("10"), null,
                null, null, null, null))).containsExactly(1L);
        assertThat(id(servis.filtrirajJela(new BigDecimal("320"), new BigDecimal("320"), null, null,
                null, null, null, null))).containsExactly(1L); // granice su uključive
        assertThat(id(servis.filtrirajJela(null, null, new BigDecimal("31"), null,
                null, null, null, null))).isEmpty();
    }

    @Test
    void cestoJedenoOgranicavaBrojINeKoristiDaljeOd60Dana() {
        when(konzumacijaRepository.findCestoJedeno(anyLong(), any(), any())).thenReturn(List.of());

        servis.dohvatiCestoJedeno(7L, 0);
        servis.dohvatiCestoJedeno(7L, 500);

        ArgumentCaptor<Limit> limit = ArgumentCaptor.forClass(Limit.class);
        verify(konzumacijaRepository, org.mockito.Mockito.times(2))
                .findCestoJedeno(eq(7L), eq(LocalDate.now(ObrokService.ZAGREB).minusDays(60)), limit.capture());
        assertThat(limit.getAllValues()).extracting(Limit::max).containsExactly(1, 50);
    }

    @Test
    void nepostojeceJelo() {
        when(jeloRepository.findById(999L)).thenReturn(Optional.empty());
        assertThat(servis.dohvatiJelo(999L)).isEmpty();
    }

    private List<Long> id(List<JeloDTO> jela) {
        return jela.stream().map(JeloDTO::getId).toList();
    }

    private Jelo jelo(Long id, String naziv, String kategorija) {
        Jelo j = mock(Jelo.class);
        when(j.getId()).thenReturn(id);
        when(j.getNaziv()).thenReturn(naziv);
        when(j.getKategorija()).thenReturn(kategorija);
        return j;
    }

    private JeloNutritivno nutritivno(Long id, String kcal, String proteini) {
        JeloNutritivno n = mock(JeloNutritivno.class);
        when(n.getIdJelo()).thenReturn(id);
        when(n.getKalorije()).thenReturn(new BigDecimal(kcal));
        when(n.getProteini()).thenReturn(new BigDecimal(proteini));
        return n;
    }
}
