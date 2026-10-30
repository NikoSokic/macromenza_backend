package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** /api/mjerenja: brzo mjerenje (jedno po danu) s novom preporukom i graf mase. */
class MjerenjeApiControllerIT extends IntegracijskiTest {

    @Test
    void brzoMjerenjeAzuriraDanasnjeIPreracunavaCilj() throws Exception {
        long k = noviKorisnik(); // registracija je već upisala današnjih 80,0 kg

        Odgovor prvo = objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":79.9}".formatted(k)).ocekuj(200);
        KotlinUgovor.provjeri(prvo.json(), "BrzoMjerenjeOdgovor");
        assertThat(broj(prvo.put("$.mjerenje.masa"))).isEqualTo(79.9);
        assertThat(prvo.<String>put("$.mjerenje.datum")).isEqualTo(DANAS.toString());
        // 10·79,9 + 1125 − 125 + 5 = 1804; ×1,55 = 2796,2
        assertThat(prvo.<Integer>put("$.preporuka.kalorije")).isEqualTo(2796);

        Odgovor drugo = objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":80.0}".formatted(k)).ocekuj(200);
        assertThat(drugo.<Number>put("$.mjerenje.id").longValue())
                .isEqualTo(prvo.<Number>put("$.mjerenje.id").longValue());

        Odgovor graf = dohvati("/api/mjerenja?idKorisnik={k}", k).ocekuj(200);
        KotlinUgovor.provjeri(graf.json(), "List<MjerenjeMase>");
        assertThat(graf.lista()).hasSize(1);
        assertThat(broj(graf.put("$[0].masa"))).isEqualTo(80.0);

        assertThat(dohvati("/api/mjerenja?idKorisnik={k}&od={od}&do={do}", k, DANAS.minusDays(10), DANAS.minusDays(1))
                .lista()).isEmpty();
    }

    @Test
    void neispravnaMasa() throws Exception {
        long k = noviKorisnik();

        assertThat(objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":19.9}".formatted(k)).ocekuj(400).poruka())
                .isEqualTo("Masa mora biti između 20 i 400 kg.");
        assertThat(objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":80.05}".formatted(k)).ocekuj(400).poruka())
                .isEqualTo("Masa smije imati najviše jednu decimalu.");
        assertThat(objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":\"puno\"}".formatted(k)).ocekuj(400).poruka())
                .startsWith("Neispravan format zahtjeva");

        java.util.UUID uid = java.util.UUID.randomUUID();
        kao(uid);
        long bezOnboardinga = objavi("/korisnici/supabase", """
                {"supabaseUid":"%s","ime":"EMPTY","prezime":"EMPTY","email":"m@macromenza.test"}"""
                .formatted(uid)).<Number>put("$.id").longValue();
        assertThat(objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":70}".formatted(bezOnboardinga))
                .ocekuj(400).poruka()).startsWith("Korisnik nema mjerenje");
    }
}
