package com.macromenza.macromenza.it;

import com.jayway.jsonpath.JsonPath;
import com.macromenza.macromenza.services.GeminiKlijent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * Snima stvarne JSON odgovore backenda kao ulaz za Android testove (frontend/app/src/test/resources/odgovori).
 * Po zadanom je isključen; pokreće se ručno:
 * <pre>.\mvnw.cmd test -Dtest=SnimiOdgovoreIT -Dsnimi.odgovore=../frontend/app/src/test/resources/odgovori</pre>
 * Koristi samo testnog korisnika u transakciji koja se poništava, pa u datotekama nema podataka pravih korisnika;
 * jela i meniji su javni podaci iz NN 120/2013.
 */
@EnabledIfSystemProperty(named = "snimi.odgovore", matches = ".+")
class SnimiOdgovoreIT extends IntegracijskiTest {

    @MockitoBean
    private GeminiKlijent gemini;

    private Path mapa;

    @Test
    void snimi() throws Exception {
        mapa = Path.of(System.getProperty("snimi.odgovore"));
        Files.createDirectories(mapa);

        long k = noviKorisnik();
        List<Map<String, Object>> jela = dohvati("/api/jela?search=grah").ocekuj(200).lista();
        Object a = jela.get(0).get("id");
        Object b = jela.get(1).get("id");

        // Jela i meniji (javni podaci)
        spremi("jela_pretraga", dohvati("/api/jela?search=grah"));
        spremi("jela_kategorije", dohvati("/api/jela/kategorije"));
        spremi("jelo_detalj", dohvati("/api/jela/{id}/detalj", a));
        Odgovor meniji = dohvati("/api/meniji?tip=RUCAK").ocekuj(200);
        spremiTekst("meniji", JsonPath.parse((Object) JsonPath.read(meniji.tijelo(), "$[0:3]")).jsonString());

        // Obroci: danas doručak i ručak, jučer večera
        spremi("obrok_spremljen", objavi("/api/obroci", obrokJson(k, DANAS, "DORUCAK", stavka(a, "1"))
                .replace("12:30", "08:00")));
        objavi("/api/obroci", obrokJson(k, DANAS, "RUCAK", stavka(a, "1.5") + "," + stavka(b, "1"))).ocekuj(200);
        objavi("/api/obroci", obrokJson(k, DANAS.minusDays(1), "VECERA", stavka(b, "0.5"))).ocekuj(200);

        spremi("obroci_dan", dohvati("/api/obroci?idKorisnik={k}&datum={d}", k, DANAS));
        spremi("dan_pregled", dohvati("/api/obroci/dan-pregled?idKorisnik={k}", k));
        spremi("tjedan", dohvati("/api/obroci/tjedan?idKorisnik={k}", k));
        spremi("tjedan_prosli", dohvati("/api/obroci/tjedan?idKorisnik={k}&pocetak={p}", k, DANAS.minusWeeks(1)));
        spremi("povijest_dana", dohvati("/api/konzumacije/povijest/{k}?datum={d}", k, DANAS));
        spremi("jela_cesto", dohvati("/api/jela/cesto?idKorisnik={k}", k));

        // Korisnik, mjerenja, preporuke
        spremi("korisnik", dohvati("/korisnici/{id}", k));
        spremi("profil", dohvati("/profil/korisnik/{id}", k));
        spremi("onboarding_status", dohvati("/korisnici/{id}/onboarding-status", k));
        spremi("mjerenja_korisnik", dohvati("/mjerenja/korisnik/{id}", k));
        spremi("preporuke_korisnik", dohvati("/preporuke/korisnik/{id}", k));
        spremi("brzo_mjerenje", objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":79.9}".formatted(k)));
        spremi("mase", dohvati("/api/mjerenja?idKorisnik={k}", k));
        spremi("izracun_pregled", objavi("/api/preporuke/pregled", """
                {"spol":"M","dob":25,"visina":180,"masa":80.0,"razinaAktivnosti":"umjerena","tipCilja":"odrzavanje"}"""));
        spremi("izvori_podataka", dohvati("/api/izvori-podataka"));

        // Prepoznavanje (Gemini je lažiran, pa je odgovor obrada backenda na izmišljen odgovor modela)
        when(gemini.imaKljuc()).thenReturn(true);
        when(gemini.getModel()).thenReturn("test-model");
        when(gemini.prepoznaj(anyString(), anyString(), any(), any())).thenReturn("""
                {"stavke":[{"idJelo":%s,"nazivSlobodno":"grah","kolicina":1.5,"sigurnost":"VISOKA"},
                           {"idJelo":%s,"nazivSlobodno":"jelo","kolicina":1,"sigurnost":"SREDNJA"},
                           {"idJelo":-5,"nazivSlobodno":"Nepoznata pita","kolicina":1,"sigurnost":"NISKA"}],
                 "napomena":"Okvirna procjena."}""".formatted(a, b));
        spremi("prijedlog_obroka", posalji(multipart("/api/prepoznavanje")
                .file(new MockMultipartFile("slika", "pladanj.png", "image/png", new byte[]{1, 2, 3}))
                .param("idKorisnik", String.valueOf(k)).param("opis", "grah i juha")));

        // Greške: tijelo {"poruka": "..."} koje aplikacija prikazuje
        spremiGresku("greska_400_kolicina", objavi("/api/obroci",
                obrokJson(k, DANAS, "RUCAK", stavka(a, "0.3"))), 400);
        spremiGresku("greska_404_korisnik", dohvati("/api/obroci/dan-pregled?idKorisnik={k}", -1), 404);
        spremiGresku("greska_400_masa", objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":19.9}".formatted(k)), 400);
    }

    private void spremi(String ime, Odgovor o) throws Exception {
        spremiTekst(ime, o.ocekuj(200).tijelo());
    }

    private void spremiGresku(String ime, Odgovor o, int status) throws Exception {
        spremiTekst(ime, o.ocekuj(status).tijelo());
    }

    private void spremiTekst(String ime, String tijelo) throws Exception {
        assertThat(tijelo).isNotBlank();
        Files.writeString(mapa.resolve(ime + ".json"), tijelo, StandardCharsets.UTF_8);
    }
}
