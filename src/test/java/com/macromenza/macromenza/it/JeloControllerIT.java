package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** /api/jela: popis, filteri, kategorije, detalj, "Često jedeš" i izračun u viewu v_jelo_nutritivno. */
class JeloControllerIT extends IntegracijskiTest {

    private static final Path PODACI = Path.of("db", "podaci");

    @Test
    void popisJelaJeUskladenSAplikacijom() throws Exception {
        Odgovor r = dohvati("/api/jela").ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "List<Jelo>");

        List<Map<String, Object>> jela = r.lista();
        assertThat(jela).hasSizeGreaterThanOrEqualTo(311);
        assertThat(jela).allSatisfy(j -> assertThat(j.get("kalorije")).as(j.get("naziv").toString()).isNotNull());
    }

    /** Svako jelo iz jela.csv: kcal iz viewa = Σ namirnica.kcal × grami / 100 (CSV) i ≈ kcal_ocekivano. */
    @Test
    void viewRacunaKalorijeIzSastojaka() throws Exception {
        Map<String, double[]> namirnice = new HashMap<>();
        for (Map<String, String> n : csv("namirnice.csv")) {
            namirnice.put(n.get("kljuc"), new double[]{
                    d(n.get("kcal_100g")), d(n.get("proteini_100g")), d(n.get("ugljikohidrati_100g")), d(n.get("masti_100g"))});
        }

        Map<String, double[]> izracun = new HashMap<>();
        for (Map<String, String> s : csv("jelo_sastojak.csv")) {
            double[] n = namirnice.get(s.get("namirnica_kljuc"));
            assertThat(n).as("namirnica " + s.get("namirnica_kljuc")).isNotNull();
            double[] zbroj = izracun.computeIfAbsent(s.get("jelo_id"), k -> new double[4]);
            for (int i = 0; i < 4; i++) {
                zbroj[i] += n[i] * d(s.get("grami")) / 100;
            }
        }

        Map<String, Map<String, Object>> poNazivu = dohvati("/api/jela").ocekuj(200).lista().stream()
                .collect(Collectors.toMap(j -> (String) j.get("naziv"), Function.identity(), (a, b) -> a));

        List<Map<String, String>> jela = csv("jela.csv");
        assertThat(jela).hasSize(311);
        List<String> greske = new ArrayList<>();

        for (Map<String, String> j : jela) {
            Map<String, Object> api = poNazivu.get(j.get("naziv"));
            if (api == null) {
                greske.add(j.get("naziv") + ": nema ga među aktivnim jelima");
                continue;
            }
            double[] o = izracun.getOrDefault(j.get("id"), new double[4]);
            String[] polja = {"kalorije", "proteini", "ugljikohidrati", "masti"};
            for (int i = 0; i < 4; i++) {
                if (Math.abs(broj(api.get(polja[i])) - o[i]) > 0.011) {
                    greske.add("%s.%s: API %s, CSV %.2f".formatted(j.get("naziv"), polja[i], api.get(polja[i]), o[i]));
                }
            }
            if (Math.abs(o[0] - d(j.get("kcal_ocekivano"))) > 1) {
                greske.add("%s: izračun %.1f, kcal_ocekivano %s".formatted(j.get("naziv"), o[0], j.get("kcal_ocekivano")));
            }
        }
        assertThat(greske).as("odstupanja").isEmpty();
    }

    @Test
    void detaljJelaZbrajaSastojke() throws Exception {
        Map<String, Object> jelo = aktivnaJela(1).get(0);

        Odgovor r = dohvati("/api/jela/{id}/detalj", jelo.get("id")).ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "JeloDetalj");

        List<Map<String, Object>> sastojci = r.put("$.sastojci");
        assertThat(sastojci).isNotEmpty();
        assertThat(sastojci.stream().mapToDouble(s -> broj(s.get("kalorije"))).sum())
                .isCloseTo(broj(r.put("$.kalorije")), within(0.05 * sastojci.size()));
        assertThat(sastojci.stream().mapToDouble(s -> broj(s.get("grami"))).sum())
                .isCloseTo(broj(r.put("$.masaG")), within(0.01));
        // Sortirano po gramima silazno
        List<Double> grami = sastojci.stream().map(s -> broj(s.get("grami"))).toList();
        assertThat(grami).isSortedAccordingTo((a, b) -> Double.compare(b, a));

        dohvati("/api/jela/{id}/detalj", -1).ocekuj(404);
    }

    @Test
    void kategorijeIFilteri() throws Exception {
        Odgovor k = dohvati("/api/jela/kategorije").ocekuj(200);
        KotlinUgovor.provjeri(k.json(), "List<KategorijaJela>");
        List<Map<String, Object>> kategorije = k.lista();
        assertThat(kategorije).isNotEmpty();

        Map<String, Object> prva = kategorije.get(0);
        List<Map<String, Object>> uKategoriji = dohvati("/api/jela?kategorija={k}", prva.get("naziv")).lista();
        assertThat(uKategoriji).hasSize(((Number) prva.get("brojJela")).intValue())
                .allSatisfy(j -> assertThat(j.get("kategorija")).isEqualTo(prva.get("naziv")));
        assertThat(kategorije.stream().mapToLong(x -> ((Number) x.get("brojJela")).longValue()).sum())
                .isEqualTo(dohvati("/api/jela").lista().size());

        assertThat(dohvati("/api/jela?search=juha").lista()).isNotEmpty()
                .allSatisfy(j -> assertThat(((String) j.get("naziv")).toLowerCase()).contains("juha"));

        assertThat(dohvati("/api/jela?minProteini=20&maxKalorije=500").lista()).isNotEmpty().allSatisfy(j -> {
            assertThat(broj(j.get("proteini"))).isGreaterThanOrEqualTo(20);
            assertThat(broj(j.get("kalorije"))).isLessThanOrEqualTo(500);
        });
    }

    @Test
    void cestoJedenoBrojiObrokeKorisnika() throws Exception {
        long korisnik = noviKorisnik();
        List<Map<String, Object>> jela = aktivnaJela(2);
        Object a = jela.get(0).get("id");
        Object b = jela.get(1).get("id");

        assertThat(dohvati("/api/jela/cesto?idKorisnik={k}", korisnik).ocekuj(200).lista()).isEmpty();

        objavi("/api/obroci", obrokJson(korisnik, DANAS, "RUCAK", stavka(a, "1") + "," + stavka(b, "1"))).ocekuj(200);
        objavi("/api/obroci", obrokJson(korisnik, DANAS.minusDays(1), "VECERA", stavka(a, "0.5"))).ocekuj(200);

        Odgovor r = dohvati("/api/jela/cesto?idKorisnik={k}", korisnik).ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "List<CestoJedenoJelo>");
        assertThat(r.<List<Object>>put("$[*].jelo.id")).containsExactly(a, b);
        assertThat(r.<Integer>put("$[0].brojPuta")).isEqualTo(2);
        assertThat(r.<String>put("$[0].zadnjiPut")).isEqualTo(DANAS.toString());

        assertThat(dohvati("/api/jela/cesto?idKorisnik={k}&limit=1", korisnik).lista()).hasSize(1);
    }

    static List<Map<String, String>> csv(String datoteka) throws IOException {
        List<String> redovi = Files.readAllLines(PODACI.resolve(datoteka), StandardCharsets.UTF_8);
        String[] zaglavlje = redovi.get(0).split(",", -1);
        List<Map<String, String>> rezultat = new ArrayList<>();
        for (String red : redovi.subList(1, redovi.size())) {
            if (red.isBlank()) continue;
            String[] v = podijeli(red);
            Map<String, String> m = new HashMap<>();
            for (int i = 0; i < zaglavlje.length; i++) {
                m.put(zaglavlje[i], i < v.length ? v[i] : "");
            }
            rezultat.add(m);
        }
        return rezultat;
    }

    /** CSV red s navodnicima ("a, b"). */
    private static String[] podijeli(String red) {
        List<String> dijelovi = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean navodnik = false;
        for (char c : red.toCharArray()) {
            if (c == '"') navodnik = !navodnik;
            else if (c == ',' && !navodnik) {
                dijelovi.add(sb.toString());
                sb.setLength(0);
            } else sb.append(c);
        }
        dijelovi.add(sb.toString());
        return dijelovi.toArray(String[]::new);
    }

    private static double d(String s) {
        return s == null || s.isBlank() ? 0 : new BigDecimal(s.trim()).doubleValue();
    }
}
