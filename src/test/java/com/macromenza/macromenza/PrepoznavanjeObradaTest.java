package com.macromenza.macromenza;

import com.macromenza.macromenza.dto.PrijedlogObrokaDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.KorisnikRepository;
import com.macromenza.macromenza.services.GeminiKlijent;
import com.macromenza.macromenza.services.PrepoznavanjeException;
import com.macromenza.macromenza.services.PrepoznavanjeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Naknadna obrada odgovora modela i provjere zahtjeva; bez stvarnog poziva Geminija i bez baze. */
class PrepoznavanjeObradaTest {

    private JeloRepository jeloRepository;
    private JeloNutritivnoRepository nutritivnoRepository;
    private KorisnikRepository korisnikRepository;
    private GeminiKlijent gemini;
    private PrepoznavanjeService servis;

    @BeforeEach
    void postavi() {
        jeloRepository = mock(JeloRepository.class);
        nutritivnoRepository = mock(JeloNutritivnoRepository.class);
        korisnikRepository = mock(KorisnikRepository.class);
        gemini = mock(GeminiKlijent.class);

        // 1 = Pohana piletina (aktivno), 2 = Kruh (aktivno), 3 = Staro jelo (neaktivno)
        Jelo piletina = jelo(1L, "Pohana piletina", true);
        Jelo kruh = jelo(2L, "Kruh", true);
        Jelo staro = jelo(3L, "Staro jelo", false);

        when(jeloRepository.findAllById(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return List.of(piletina, kruh, staro).stream().filter(j -> ids.contains(j.getId())).toList();
        });

        when(nutritivnoRepository.findAllById(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return List.of(
                    nutritivno(1L, "300", "25", "10", "15"),
                    nutritivno(2L, "80", "3", "15", "1")
            ).stream().filter(n -> ids.contains(n.getIdJelo())).toList();
        });

        when(korisnikRepository.existsById(anyLong())).thenReturn(true);
        when(gemini.imaKljuc()).thenReturn(true);

        servis = new PrepoznavanjeService(jeloRepository, nutritivnoRepository, korisnikRepository, gemini, "test-model");
    }

    @Test
    void nepostojeciIdIdeUNeprepoznato() {
        PrijedlogObrokaDTO p = servis.obradi("""
                {"stavke":[
                  {"idJelo":999,"nazivSlobodno":"Čudno jelo","kolicina":1,"sigurnost":"VISOKA"},
                  {"idJelo":1,"nazivSlobodno":"piletina","kolicina":1,"sigurnost":"VISOKA"}
                ],"napomena":""}""");

        assertThat(p.getStavke()).hasSize(1);
        assertThat(p.getStavke().get(0).getIdJelo()).isEqualTo(1L);
        assertThat(p.getNeprepoznato()).containsExactly("Čudno jelo");
        assertThat(p.getModel()).isEqualTo("test-model");
    }

    @Test
    void neaktivnoJeloIIdNullIduUNeprepoznato() {
        PrijedlogObrokaDTO p = servis.obradi("""
                {"stavke":[
                  {"idJelo":3,"nazivSlobodno":"Staro jelo","kolicina":1,"sigurnost":"SREDNJA"},
                  {"idJelo":null,"nazivSlobodno":"Sok od jabuke","kolicina":1,"sigurnost":"NISKA"}
                ],"napomena":"nejasno"}""");

        assertThat(p.getStavke()).isEmpty();
        assertThat(p.getNeprepoznato()).containsExactly("Staro jelo", "Sok od jabuke");
        assertThat(p.getNapomena()).isEqualTo("nejasno");
    }

    @Test
    void kolicinaSeZaokruzujeNaPolaPorcije() {
        PrijedlogObrokaDTO p = servis.obradi("""
                {"stavke":[
                  {"idJelo":1,"nazivSlobodno":"a","kolicina":1.3,"sigurnost":"SREDNJA"},
                  {"idJelo":2,"nazivSlobodno":"b","kolicina":0.2,"sigurnost":"SREDNJA"}
                ],"napomena":""}""");

        assertThat(p.getStavke().get(0).getKolicina()).isEqualByComparingTo("1.5");
        assertThat(p.getStavke().get(1).getKolicina()).isEqualByComparingTo("0.5");
    }

    @Test
    void vrijednostiSuIzBazeSKolicinom() {
        PrijedlogObrokaDTO p = servis.obradi("""
                {"stavke":[{"idJelo":1,"nazivSlobodno":"a","kolicina":1.5,"sigurnost":"VISOKA"}],"napomena":""}""");

        PrijedlogObrokaDTO.Stavka s = p.getStavke().get(0);
        assertThat(s.getNaziv()).isEqualTo("Pohana piletina");
        assertThat(s.getKalorije()).isEqualByComparingTo("450.00");
        assertThat(s.getProteini()).isEqualByComparingTo("37.50");
        assertThat(s.getUgljikohidrati()).isEqualByComparingTo("15.00");
        assertThat(s.getMasti()).isEqualByComparingTo("22.50");
    }

    @Test
    void duplikatiSeSpajajuSNajnizomSigurnosti() {
        PrijedlogObrokaDTO p = servis.obradi("""
                {"stavke":[
                  {"idJelo":2,"nazivSlobodno":"Kruh","kolicina":1,"sigurnost":"VISOKA"},
                  {"idJelo":1,"nazivSlobodno":"Piletina","kolicina":1,"sigurnost":"VISOKA"},
                  {"idJelo":2,"nazivSlobodno":"Kruh","kolicina":2,"sigurnost":"NISKA"}
                ],"napomena":""}""");

        assertThat(p.getStavke()).hasSize(2);
        PrijedlogObrokaDTO.Stavka kruh = p.getStavke().get(0);
        assertThat(kruh.getIdJelo()).isEqualTo(2L);
        assertThat(kruh.getKolicina()).isEqualByComparingTo("3");
        assertThat(kruh.getSigurnost()).isEqualTo("NISKA");
        assertThat(kruh.getKalorije()).isEqualByComparingTo("240.00");
    }

    @Test
    void prazanOdgovorBezStavkiDajePrazanPrijedlog() {
        PrijedlogObrokaDTO p = servis.obradi("{}");

        assertThat(p.getStavke()).isEmpty();
        assertThat(p.getNeprepoznato()).isEmpty();
        assertThat(p.getNapomena()).isNull();
    }

    @Test
    void praznoIliNeispravanJsonJe502() {
        for (String json : new String[]{"", "   ", null, "nije json", "[1,2]"}) {
            assertThatThrownBy(() -> servis.obradi(json))
                    .isInstanceOfSatisfying(PrepoznavanjeException.class,
                            e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY));
        }
    }

    @Test
    void zahtjevBezSlikeIOpisaJe400() {
        assertThatThrownBy(() -> servis.prepoznaj(1L, null, "  "))
                .isInstanceOfSatisfying(PrepoznavanjeException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void neispravanTipSlikeJe400() {
        MockMultipartFile gif = new MockMultipartFile("slika", "a.gif", "image/gif", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> servis.prepoznaj(1L, gif, null))
                .isInstanceOfSatisfying(PrepoznavanjeException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void prevelikaSlikaJe400() {
        MockMultipartFile velika = new MockMultipartFile(
                "slika", "a.jpg", "image/jpeg", new byte[8 * 1024 * 1024 + 1]);

        assertThatThrownBy(() -> servis.prepoznaj(1L, velika, null))
                .isInstanceOfSatisfying(PrepoznavanjeException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void bezKljucaJe503() {
        when(gemini.imaKljuc()).thenReturn(false);

        assertThatThrownBy(() -> servis.prepoznaj(1L, null, "kruh"))
                .isInstanceOfSatisfying(PrepoznavanjeException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(e.getMessage()).isEqualTo("Prepoznavanje nije podešeno");
                });
    }

    @Test
    void dvadesetPrvoPrepoznavanjeDnevnoJe429() {
        when(jeloRepository.findByAktivnoTrueOrderByNaziv()).thenReturn(List.of());
        when(gemini.prepoznaj(any(), any(), any(), any())).thenReturn("{\"stavke\":[],\"napomena\":\"\"}");

        for (int i = 0; i < 20; i++) {
            servis.prepoznaj(1L, null, "kruh");
        }

        assertThatThrownBy(() -> servis.prepoznaj(1L, null, "kruh"))
                .isInstanceOfSatisfying(PrepoznavanjeException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));

        // Drugi korisnik ima vlastiti brojač.
        assertThat(servis.prepoznaj(2L, null, "kruh").getStavke()).isEmpty();
    }

    private Jelo jelo(Long id, String naziv, boolean aktivno) {
        Jelo j = mock(Jelo.class);
        when(j.getId()).thenReturn(id);
        when(j.getNaziv()).thenReturn(naziv);
        when(j.getAktivno()).thenReturn(aktivno);
        return j;
    }

    private JeloNutritivno nutritivno(Long id, String kcal, String p, String uh, String m) {
        JeloNutritivno n = mock(JeloNutritivno.class);
        when(n.getIdJelo()).thenReturn(id);
        when(n.getKalorije()).thenReturn(new BigDecimal(kcal));
        when(n.getProteini()).thenReturn(new BigDecimal(p));
        when(n.getUgljikohidrati()).thenReturn(new BigDecimal(uh));
        when(n.getMasti()).thenReturn(new BigDecimal(m));
        return n;
    }
}
