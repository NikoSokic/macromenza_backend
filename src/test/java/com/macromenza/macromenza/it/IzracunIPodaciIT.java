package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Pregled izračuna (onboarding), "Odakle su kalorije" i brisanje svih podataka. */
class IzracunIPodaciIT extends IntegracijskiTest {

    private static final String ULAZ = """
            {"spol":"M","dob":25,"visina":180,"masa":80.0,"razinaAktivnosti":"umjerena","tipCilja":"odrzavanje"}""";

    @Test
    void pregledIzracunaNistaNeSprema() throws Exception {
        Odgovor r = objavi("/api/preporuke/pregled", ULAZ).ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "IzracunPreporuke");
        assertThat(r.<Integer>put("$.ciljKalorije")).isEqualTo(2798);
        assertThat(r.<String>put("$.bmrFormula")).isEqualTo("10 × 80 + 6,25 × 180 − 5 × 25 + 5");
    }

    @Test
    void pregledIzracunaProvjeravaUlaze() throws Exception {
        Map<String, String> slucajevi = Map.of(
                "\"spol\":\"M\"", "Spol mora biti M ili Z.",
                "\"dob\":25", "Dob mora biti između 15 i 100 godina.",
                "\"visina\":180", "Visina mora biti između 100 i 250 cm.",
                "\"masa\":80.0", "Masa mora biti između 20 i 400 kg.",
                "\"razinaAktivnosti\":\"umjerena\"", "Razina aktivnosti mora biti sjedilacka, lagana, umjerena ili visoka.",
                "\"tipCilja\":\"odrzavanje\"", "Tip cilja mora biti mrsavljenje, odrzavanje ili dobivanje_mase.");
        Map<String, String> krivo = Map.of(
                "\"spol\":\"M\"", "\"spol\":\"X\"",
                "\"dob\":25", "\"dob\":14",
                "\"visina\":180", "\"visina\":251",
                "\"masa\":80.0", "\"masa\":401",
                "\"razinaAktivnosti\":\"umjerena\"", "\"razinaAktivnosti\":\"ekstremna\"",
                "\"tipCilja\":\"odrzavanje\"", "\"tipCilja\":\"bulk\"");

        for (var e : slucajevi.entrySet()) {
            assertThat(objavi("/api/preporuke/pregled", ULAZ.replace(e.getKey(), krivo.get(e.getKey())))
                    .ocekuj(400).poruka()).isEqualTo(e.getValue());
        }
    }

    @Test
    void izvoriPodataka() throws Exception {
        Odgovor r = dohvati("/api/izvori-podataka").ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "IzvoriPodataka");
        assertThat(r.<Integer>put("$.brojJela")).isEqualTo(dohvati("/api/jela").lista().size());
        assertThat(r.<Integer>put("$.brojNamirnica")).isEqualTo(JeloControllerIT.csv("namirnice.csv").size()).isEqualTo(182);
        assertThat(r.<Integer>put("$.brojMenija")).isEqualTo(84);
        assertThat(r.<String>put("$.izvorNormativa")).isEqualTo("NN 120/2013");
    }

    @Test
    void brisanjeSvihPodatakaOstavljaRacunIProfil() throws Exception {
        long k = noviKorisnik();
        Object jelo = aktivnaJela(1).get(0).get("id");
        objavi("/api/obroci", obrokJson(k, DANAS, "RUCAK", stavka(jelo, "1") + "," + stavka(jelo, "0.5"))).ocekuj(200);
        objavi("/api/obroci", obrokJson(k, DANAS, "VECERA", stavka(jelo, "1"))).ocekuj(200);

        assertThat(obrisi("/api/korisnici/{id}/podaci", k).ocekuj(400).poruka()).contains("OBRISI");
        assertThat(obrisi("/api/korisnici/{id}/podaci?potvrda=da", k).status()).isEqualTo(400);

        Map<String, Object> obrisano = obrisi("/api/korisnici/{id}/podaci?potvrda=OBRISI", k).ocekuj(200).objekt();
        assertThat(obrisano).containsEntry("obrok", 2).containsEntry("konzumacija", 3)
                .containsEntry("mjerenje", 1).containsEntry("preporuka", 1);

        assertThat(dohvati("/api/obroci?idKorisnik={k}&datum={d}", k, DANAS).lista()).isEmpty();
        assertThat(dohvati("/api/obroci/dan-pregled?idKorisnik={k}", k).<Object>put("$.cilj")).isNull();
        assertThat(dohvati("/mjerenja/korisnik/{id}", k).lista()).isEmpty();
        dohvati("/korisnici/{id}", k).ocekuj(200);
        assertThat(dohvati("/profil/korisnik/{id}", k).<Integer>put("$.visina")).isEqualTo(180);

        obrisi("/api/korisnici/{id}/podaci?potvrda=OBRISI", -1).ocekuj(404);
    }
}
