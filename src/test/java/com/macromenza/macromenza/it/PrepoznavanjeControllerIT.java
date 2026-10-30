package com.macromenza.macromenza.it;

import com.macromenza.macromenza.services.GeminiKlijent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/** POST /api/prepoznavanje s lažnim Gemini klijentom: nikad se ne zove pravi API ni ne troši kvota. */
class PrepoznavanjeControllerIT extends IntegracijskiTest {

    @MockitoBean
    private GeminiKlijent gemini;

    private long korisnik;
    private Map<String, Object> jelo;

    @BeforeEach
    void postavi() throws Exception {
        korisnik = noviKorisnik();
        jelo = aktivnaJela(1).get(0);
        when(gemini.imaKljuc()).thenReturn(true);
        when(gemini.getModel()).thenReturn("test-model");
        when(gemini.prepoznaj(anyString(), anyString(), any(), any())).thenReturn("""
                {"stavke":[{"idJelo":%s,"nazivSlobodno":"jelo","kolicina":1.5,"sigurnost":"VISOKA"},
                           {"idJelo":-5,"nazivSlobodno":"Nepoznata pita","kolicina":1,"sigurnost":"NISKA"}],
                 "napomena":"Okvirna procjena."}""".formatted(jelo.get("id")));
    }

    @Test
    void opisDajePrijedlogKojiSeMozeSpremitiKaoObrok() throws Exception {
        Odgovor r = posalji(multipart("/api/prepoznavanje")
                .param("idKorisnik", String.valueOf(korisnik))
                .param("opis", "pola porcije grah varivo i kruh"));
        r.ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "PrijedlogObroka");

        assertThat(r.<Object>put("$.stavke[0].idJelo")).isEqualTo(jelo.get("id"));
        assertThat(broj(r.put("$.stavke[0].kalorije"))).isEqualTo(broj(jelo.get("kalorije")) * 1.5,
                org.assertj.core.api.Assertions.within(0.02));
        assertThat(r.<java.util.List<String>>put("$.neprepoznato")).containsExactly("Nepoznata pita");
        verify(gemini).prepoznaj(anyString(), anyString(), isNull(), isNull());

        // Potvrda prijedloga ide kroz obični POST /api/obroci s izvorom OPIS.
        objavi("/api/obroci", """
                {"idKorisnik":%d,"datum":"%s","vrijeme":"13:00","tipObroka":"RUCAK","izvor":"OPIS",
                 "stavke":[{"idJelo":%s,"kolicina":1.5}]}""".formatted(korisnik, DANAS, jelo.get("id"))).ocekuj(200);
    }

    @Test
    void slikaSeSaljeModelu() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        posalji(multipart("/api/prepoznavanje")
                .file(new MockMultipartFile("slika", "pladanj.png", "image/png", png))
                .param("idKorisnik", String.valueOf(korisnik))).ocekuj(200);

        verify(gemini).prepoznaj(anyString(), anyString(), org.mockito.ArgumentMatchers.eq(png),
                org.mockito.ArgumentMatchers.eq("image/png"));
    }

    @Test
    void neispravniZahtjeviNeDolazeDoModela() throws Exception {
        assertThat(posalji(multipart("/api/prepoznavanje").param("idKorisnik", String.valueOf(korisnik)))
                .ocekuj(400).poruka()).isEqualTo("Pošalji sliku ili opis obroka.");
        assertThat(posalji(multipart("/api/prepoznavanje").param("opis", "grah")).ocekuj(400).poruka())
                .isEqualTo("idKorisnik je obavezan.");
        assertThat(posalji(multipart("/api/prepoznavanje")
                .file(new MockMultipartFile("slika", "a.gif", "image/gif", new byte[]{1}))
                .param("idKorisnik", String.valueOf(korisnik))).ocekuj(400).poruka())
                .isEqualTo("Slika mora biti JPEG, PNG ili WebP.");
        posalji(multipart("/api/prepoznavanje").param("idKorisnik", "-1").param("opis", "grah")).ocekuj(403);

        when(gemini.imaKljuc()).thenReturn(false);
        posalji(multipart("/api/prepoznavanje").param("idKorisnik", String.valueOf(korisnik)).param("opis", "grah"))
                .ocekuj(503);

        verify(gemini, never()).prepoznaj(anyString(), anyString(), any(), any());
    }
}
