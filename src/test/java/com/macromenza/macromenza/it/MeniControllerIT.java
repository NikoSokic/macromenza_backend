package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** /api/meniji: 84 menija, filteri i viewovi v_meni_stavka_nutritivno / v_meni_nutritivno. */
class MeniControllerIT extends IntegracijskiTest {

    @Test
    void sviMenijiIUgovor() throws Exception {
        Odgovor r = dohvati("/api/meniji").ocekuj(200);
        KotlinUgovor.provjeri(r.json(), "List<MeniDTO>");

        List<Map<String, Object>> meniji = r.lista();
        assertThat(meniji).hasSize(JeloControllerIT.csv("meniji.csv").size()).hasSize(84);
        assertThat(r.<List<?>>put("$[*].stavke[*]")).hasSize(JeloControllerIT.csv("meni_stavke.csv").size());
    }

    @Test
    void totalMenijaJeZbrojStavkiIStavkaJeJeloPutaKolicina() throws Exception {
        Map<Object, Double> kcalJela = new java.util.HashMap<>();
        for (Map<String, Object> j : dohvati("/api/jela").lista()) {
            kcalJela.put(j.get("id"), broj(j.get("kalorije")));
        }

        for (Map<String, Object> m : dohvati("/api/meniji").lista()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stavke = (List<Map<String, Object>>) m.get("stavke");
            double zbroj = 0;
            for (Map<String, Object> s : stavke) {
                double ocekivano = kcalJela.get(s.get("idJelo")) * broj(s.get("kolicina"));
                assertThat(broj(s.get("kalorije"))).as(m.get("sifra") + " / " + s.get("nazivJela"))
                        .isCloseTo(ocekivano, within(0.01));
                zbroj += broj(s.get("kalorije"));
            }
            // Stavke su zaokružene zasebno, total iz nezaokruženog zbroja.
            assertThat(broj(m.get("kalorije"))).as((String) m.get("sifra"))
                    .isCloseTo(zbroj, within(0.01 * stavke.size()));
        }
    }

    @Test
    void filtriPoTipuIVegetarijanskom() throws Exception {
        List<Map<String, Object>> rucci = dohvati("/api/meniji?tip=rucak").ocekuj(200).lista();
        assertThat(rucci).isNotEmpty().allSatisfy(m -> assertThat(m.get("tip")).isEqualTo("RUCAK"));

        List<Map<String, Object>> vege = dohvati("/api/meniji?tip=VECERA&vegetarijanski=true").ocekuj(200).lista();
        assertThat(vege).isNotEmpty().allSatisfy(m -> {
            assertThat(m.get("tip")).isEqualTo("VECERA");
            assertThat(m.get("vegetarijanski")).isEqualTo(true);
        });

        int ukupno = dohvati("/api/meniji").lista().size();
        int poTipu = 0;
        for (String tip : List.of("DORUCAK", "RUCAK", "UZINA", "VECERA")) {
            poTipu += dohvati("/api/meniji?tip={t}", tip).lista().size();
        }
        assertThat(poTipu).isEqualTo(ukupno);
    }

    @Test
    void jedanMeni() throws Exception {
        Map<String, Object> prvi = dohvati("/api/meniji").lista().get(0);
        assertThat(dohvati("/api/meniji/{id}", prvi.get("id")).ocekuj(200).objekt()).isEqualTo(prvi);
        dohvati("/api/meniji/{id}", -1).ocekuj(404);
    }
}
