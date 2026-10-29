package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Povijest dana (još je koriste Home, unos obroka i kartica "Dan") i zastarjeli endpointi koji moraju raditi. */
class KonzumacijaControllerIT extends IntegracijskiTest {

    @Test
    void povijestDanaGrupiraPoTipuIZbraja() throws Exception {
        long k = noviKorisnik();
        List<Map<String, Object>> jela = aktivnaJela(2);
        Number o1 = objavi("/api/obroci", obrokJson(k, DANAS, "DORUCAK", stavka(jela.get(0).get("id"), "1")))
                .ocekuj(200).put("$.ukupnoKalorije");
        Number o2 = objavi("/api/obroci", obrokJson(k, DANAS, "VECERA", stavka(jela.get(1).get("id"), "1.5")))
                .ocekuj(200).put("$.ukupnoKalorije");

        Odgovor r = dohvati("/api/konzumacije/povijest/{k}?datum={d}", k, DANAS).ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "PovijestDana");
        assertThat(r.<String>put("$.datum")).isEqualTo(DANAS.toString());
        assertThat(broj(r.put("$.ukupnoKalorije"))).isCloseTo(broj(o1) + broj(o2), within(0.02));
        assertThat(r.<Map<String, Object>>put("$.obroci")).containsKeys("DORUCAK", "VECERA");
        assertThat(broj(r.put("$.obroci.VECERA[0].kolicina"))).isEqualTo(1.5);

        Odgovor prazan = dohvati("/api/konzumacije/povijest/{k}?datum={d}", k, DANAS.minusDays(3)).ocekuj(200);
        assertThat(broj(prazan.put("$.ukupnoKalorije"))).isZero();
    }

    @Test
    @SuppressWarnings("deprecation")
    void zastarjeliEndpointiJosOdgovaraju() throws Exception {
        long k = noviKorisnik();
        objavi("/api/obroci", obrokJson(k, DANAS, "RUCAK", stavka(aktivnaJela(1).get(0).get("id"), "1"))).ocekuj(200);

        dohvati("/api/dashboard/{k}", k).ocekuj(200);
        dohvati("/api/konzumacije/ukupno/{k}", k).ocekuj(200);
        assertThat(dohvati("/api/konzumacije/povijest/dani/{k}", k).ocekuj(200).<List<String>>put("$"))
                .contains(DANAS.toString());
        dohvati("/api/konzumacije/tjedni-pregled/{k}", k).ocekuj(200);
        assertThat(dohvati("/api/konzumacije/korisnik/{k}", k).ocekuj(200).lista()).hasSize(1);
    }
}
