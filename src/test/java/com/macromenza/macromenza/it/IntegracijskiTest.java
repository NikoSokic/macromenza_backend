package com.macromenza.macromenza.it;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Integracijski testovi na pravoj bazi (iste postavke kao aplikacija). Svaki test radi u transakciji koja se
 * na kraju poništava, pa u bazi ne ostaje ništa: testni korisnik, obroci i mjerenja nestaju s rollbackom.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class IntegracijskiTest {

    static final LocalDate DANAS = LocalDate.now(ZoneId.of("Europe/Zagreb"));

    @Autowired
    protected MockMvc mvc;

    /** Odgovor zahtjeva: HTTP status i JSON (Map/List/vrijednost ili null kad je tijelo prazno). */
    record Odgovor(int status, String tijelo) {
        Object json() {
            return tijelo.isBlank() ? null : JsonPath.parse(tijelo).json();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> objekt() {
            return (Map<String, Object>) json();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista() {
            return (List<Map<String, Object>>) json();
        }

        <T> T put(String jsonPath) {
            return JsonPath.read(tijelo, jsonPath);
        }

        String poruka() {
            return put("$.poruka");
        }

        Odgovor ocekuj(int ocekivaniStatus) {
            assertThat(status).as("HTTP status, tijelo: " + tijelo).isEqualTo(ocekivaniStatus);
            return this;
        }
    }

    Odgovor posalji(RequestBuilder zahtjev) throws Exception {
        MvcResult r = mvc.perform(zahtjev).andReturn();
        return new Odgovor(r.getResponse().getStatus(), r.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    Odgovor dohvati(String url, Object... varijable) throws Exception {
        return posalji(get(url, varijable));
    }

    Odgovor objavi(String url, String json, Object... varijable) throws Exception {
        return posalji(post(url, varijable).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    Odgovor zamijeni(String url, String json, Object... varijable) throws Exception {
        return posalji(put(url, varijable).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    Odgovor obrisi(String url, Object... varijable) throws Exception {
        return posalji(delete(url, varijable));
    }

    /** Novi korisnik kroz isti endpoint kao registracija u aplikaciji (M, 25, 180 cm, 80 kg → cilj 2798 kcal). */
    long noviKorisnik() throws Exception {
        return objavi("/korisnici/registracija-profil", """
                {"supabaseUid":"%s","email":"test-%s@macromenza.test","ime":"Test","prezime":"Korisnik",
                 "spol":"M","visina":180,"dob":25,"masa":80.0,"razinaAktivnosti":"umjerena","tipCilja":"odrzavanje"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID()))
                .ocekuj(200)
                .<Number>put("$.id").longValue();
    }

    /** Prvih [n] aktivnih jela s vrijednostima (id, kalorije). */
    List<Map<String, Object>> aktivnaJela(int n) throws Exception {
        List<Map<String, Object>> jela = dohvati("/api/jela?minKalorije=50").ocekuj(200).lista();
        assertThat(jela).hasSizeGreaterThanOrEqualTo(n);
        return jela.subList(0, n);
    }

    String obrokJson(long idKorisnik, LocalDate datum, String tip, String stavke) {
        return """
                {"idKorisnik":%d,"datum":"%s","vrijeme":"12:30","tipObroka":"%s","stavke":[%s]}
                """.formatted(idKorisnik, datum, tip, stavke);
    }

    static String stavka(Object idJelo, String kolicina) {
        return "{\"idJelo\":%s,\"kolicina\":%s}".formatted(idJelo, kolicina);
    }

    static double broj(Object o) {
        return ((Number) o).doubleValue();
    }
}
