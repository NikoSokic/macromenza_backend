package com.macromenza.macromenza.it;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** /api/obroci: spremanje, dohvat, uređivanje, brisanje, pregled dana, tjedan, primjer tjedna. */
class ObrokControllerIT extends IntegracijskiTest {

    private long korisnik;
    private Map<String, Object> jeloA;
    private Map<String, Object> jeloB;

    @BeforeEach
    void postavi() throws Exception {
        korisnik = noviKorisnik();
        List<Map<String, Object>> jela = aktivnaJela(2);
        jeloA = jela.get(0);
        jeloB = jela.get(1);
    }

    @Test
    void cijeliZivotniCiklusObroka() throws Exception {
        // Spremanje u jednom pozivu
        Odgovor spremljen = objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK",
                stavka(jeloA.get("id"), "1.5") + "," + stavka(jeloB.get("id"), "1"))).ocekuj(200);
        KotlinUgovor.provjeri(spremljen.json(), "Obrok");

        Number id = spremljen.put("$.id");
        double ocekivano = broj(jeloA.get("kalorije")) * 1.5 + broj(jeloB.get("kalorije"));
        assertThat(broj(spremljen.put("$.ukupnoKalorije"))).isCloseTo(ocekivano, within(0.02));
        assertThat(spremljen.<String>put("$.izvor")).isEqualTo("RUCNO");
        assertThat(spremljen.<String>put("$.vrijeme")).isEqualTo("12:30");
        assertThat(spremljen.<List<?>>put("$.stavke")).hasSize(2);

        // Dohvat dana i jednog obroka
        Odgovor dan = dohvati("/api/obroci?idKorisnik={k}&datum={d}", korisnik, DANAS).ocekuj(200);
        KotlinUgovor.provjeri(dan.json(), "List<Obrok>");
        assertThat(dan.lista()).hasSize(1);
        KotlinUgovor.provjeri(dohvati("/api/obroci/{id}?idKorisnik={k}", id, korisnik).ocekuj(200).json(), "Obrok");

        // Uređivanje: drugi tip i samo jedno jelo pola porcije
        Odgovor ureden = zamijeni("/api/obroci/{id}?idKorisnik={k}",
                obrokJson(korisnik, DANAS, "VECERA", stavka(jeloB.get("id"), "0.5")), id, korisnik).ocekuj(200);
        KotlinUgovor.provjeri(ureden.json(), "Obrok");
        assertThat(ureden.<String>put("$.tipObroka")).isEqualTo("VECERA");
        assertThat(ureden.<List<?>>put("$.stavke")).hasSize(1);
        assertThat(broj(ureden.put("$.ukupnoKalorije"))).isCloseTo(broj(jeloB.get("kalorije")) / 2, within(0.01));

        // Brisanje (konzumacije kaskadno)
        assertThat(obrisi("/api/obroci/{id}?idKorisnik={k}", id, korisnik).status()).isEqualTo(204);
        dohvati("/api/obroci/{id}?idKorisnik={k}", id, korisnik).ocekuj(404);
        assertThat(dohvati("/api/obroci?idKorisnik={k}&datum={d}", korisnik, DANAS).lista()).isEmpty();
    }

    @Test
    void neispravniZahtjeviDaju400SPorukom() throws Exception {
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloA.get("id"), "0.3")))
                .ocekuj(400).poruka()).isEqualTo("Količina mora biti višekratnik od 0,5.");
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS.plusDays(1), "RUCAK", stavka(jeloA.get("id"), "1")))
                .ocekuj(400).poruka()).isEqualTo("Datum obroka ne može biti u budućnosti.");
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS, "MARENDA", stavka(jeloA.get("id"), "1")))
                .ocekuj(400).poruka()).startsWith("Tip obroka");
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", ""))
                .ocekuj(400).poruka()).isEqualTo("Obrok mora imati barem jedno jelo.");
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(-1, "1")))
                .ocekuj(400).poruka()).isEqualTo("Jelo -1 ne postoji.");
        assertThat(objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloA.get("id"), "1"))
                .replace("12:30", "25:99")).ocekuj(400).poruka()).startsWith("Neispravan format zahtjeva");
    }

    @Test
    void tudiObrokJeNevidljiv() throws Exception {
        Number id = objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloA.get("id"), "1")))
                .ocekuj(200).put("$.id");
        long drugi = noviKorisnik();

        dohvati("/api/obroci/{id}?idKorisnik={k}", id, drugi).ocekuj(404);
        zamijeni("/api/obroci/{id}?idKorisnik={k}",
                obrokJson(drugi, DANAS, "RUCAK", stavka(jeloA.get("id"), "2")), id, drugi).ocekuj(404);
        obrisi("/api/obroci/{id}?idKorisnik={k}", id, drugi).ocekuj(404);
        dohvati("/api/obroci/{id}?idKorisnik={k}", id, korisnik).ocekuj(200);
    }

    @Test
    void pregledDanaZaPocetniEkran() throws Exception {
        objavi("/api/obroci", obrokJson(korisnik, DANAS, "DORUCAK", stavka(jeloA.get("id"), "1"))).ocekuj(200);
        objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloB.get("id"), "2"))).ocekuj(200);

        Odgovor d = dohvati("/api/obroci/dan-pregled?idKorisnik={k}", korisnik).ocekuj(200);
        KotlinUgovor.provjeri(d.json(), "DanPregled");

        assertThat(broj(d.put("$.cilj.kalorije"))).isEqualTo(2798.0);
        assertThat(d.<List<String>>put("$.poTipu[*].tipObroka")).containsExactly("DORUCAK", "RUCAK", "UZINA", "VECERA");
        double pojedeno = broj(jeloA.get("kalorije")) + 2 * broj(jeloB.get("kalorije"));
        assertThat(broj(d.put("$.pojedeno.kalorije"))).isCloseTo(pojedeno, within(0.02));
        assertThat(d.<String>put("$.savjet.naslov")).isNotBlank();

        Odgovor nepostojeci = dohvati("/api/obroci/dan-pregled?idKorisnik={k}", -1).ocekuj(404);
        assertThat(nepostojeci.poruka()).isEqualTo("Korisnik ne postoji.");
    }

    @Test
    void tjedanZaPovijest() throws Exception {
        objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloA.get("id"), "1"))).ocekuj(200);

        Odgovor t = dohvati("/api/obroci/tjedan?idKorisnik={k}", korisnik).ocekuj(200);
        KotlinUgovor.provjeri(t.json(), "Tjedan");
        assertThat(t.<List<?>>put("$.dani")).hasSize(7);
        assertThat(t.<Integer>put("$.daniSUnosom")).isEqualTo(1);
        assertThat(t.<Boolean>put("$.imaSljedeci")).isFalse();

        Odgovor prosli = dohvati("/api/obroci/tjedan?idKorisnik={k}&pocetak={p}", korisnik, DANAS.minusWeeks(1))
                .ocekuj(200);
        assertThat(prosli.<Boolean>put("$.imaSljedeci")).isTrue();
        assertThat(prosli.<Integer>put("$.daniSUnosom")).isZero();

        dohvati("/api/obroci/tjedan?idKorisnik={k}&pocetak={p}", korisnik, DANAS.plusWeeks(1)).ocekuj(400);
        dohvati("/api/obroci/tjedan?idKorisnik={k}", -1).ocekuj(404);
    }

    @Test
    void primjerTjednaSeUcitavaIUklanja() throws Exception {
        Map<String, Object> stvoreno = objavi("/api/obroci/primjer-tjedna?idKorisnik={k}", "", korisnik)
                .ocekuj(200).objekt();
        assertThat(stvoreno).isNotEmpty().allSatisfy((dan, broj) -> assertThat(broj).isEqualTo(3));
        assertThat(stvoreno).doesNotContainKey(DANAS.toString());

        String prviDan = stvoreno.keySet().iterator().next();
        assertThat(dohvati("/api/obroci?idKorisnik={k}&datum={d}", korisnik, prviDan).lista())
                .extracting(o -> o.get("izvor")).containsOnly("PRIMJER");

        Odgovor t = dohvati("/api/obroci/tjedan?idKorisnik={k}&pocetak={p}", korisnik, prviDan).ocekuj(200);
        assertThat(t.<Boolean>put("$.imaPrimjer")).isTrue();

        // Ponovno učitavanje ne dira dane koji već imaju obroke.
        assertThat(objavi("/api/obroci/primjer-tjedna?idKorisnik={k}", "", korisnik).ocekuj(200).objekt()).isEmpty();

        Odgovor uklonjeno = obrisi("/api/obroci/primjer-tjedna?idKorisnik={k}", korisnik).ocekuj(200);
        assertThat(uklonjeno.<Integer>put("$.obrisano")).isEqualTo(3 * stvoreno.size());
        assertThat(dohvati("/api/obroci?idKorisnik={k}&datum={d}", korisnik, prviDan).lista()).isEmpty();
    }

    @Test
    void primjerNeBriseRucneObroke() throws Exception {
        objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(jeloA.get("id"), "1"))).ocekuj(200);
        objavi("/api/obroci/primjer-tjedna?idKorisnik={k}", "", korisnik).ocekuj(200);
        obrisi("/api/obroci/primjer-tjedna?idKorisnik={k}", korisnik).ocekuj(200);

        assertThat(dohvati("/api/obroci?idKorisnik={k}&datum={d}", korisnik, DANAS).lista()).hasSize(1);
    }
}
