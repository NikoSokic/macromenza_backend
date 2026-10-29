package com.macromenza.macromenza.it;

import com.macromenza.macromenza.dto.BrzoMjerenjeRequest;
import com.macromenza.macromenza.dto.IzracunPreporukeRequest;
import com.macromenza.macromenza.dto.ObrokRequest;
import com.macromenza.macromenza.dto.RegistracijaProfilRequest;
import com.macromenza.macromenza.model.Korisnik;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.model.ProfilPodaci;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ugovor u smjeru Android → backend: svako polje koje aplikacija šalje mora imati setter u Java DTO-u
 * (inače ga Jackson tiho ignorira). Uz to provjera da sama provjera ugovora hvata greške. Bez baze.
 */
class UgovorZahtjevaTest {

    @BeforeAll
    static void frontend() {
        Assumptions.assumeTrue(KotlinUgovor.dostupan(), "frontend/ nije pored backend/");
    }

    @Test
    void zahtjeviAplikacijeImajuSvaPoljaUBackendu() {
        Map<String, Class<?>> parovi = Map.of(
                "ObrokRequest", ObrokRequest.class,
                "StavkaObrokaRequest", ObrokRequest.Stavka.class,
                "RegistracijaProfilRequest", RegistracijaProfilRequest.class,
                "IzracunPreporukeRequest", IzracunPreporukeRequest.class,
                "BrzoMjerenjeRequest", BrzoMjerenjeRequest.class,
                "Korisnik", Korisnik.class,       // PUT korisnici/{id}, POST korisnici/supabase
                "ProfilPodaci", ProfilPodaci.class, // PUT profil/{id}
                "Mjerenje", Mjerenje.class          // POST mjerenja
        );

        parovi.forEach((kotlin, java) -> {
            Set<String> setteri = Arrays.stream(java.getMethods())
                    .filter(m -> m.getName().startsWith("set") && m.getParameterCount() == 1)
                    .map(m -> Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4))
                    .collect(Collectors.toSet());
            List<String> nedostaju = KotlinUgovor.polja(kotlin).stream()
                    .filter(p -> !p.equals("id") && !setteri.contains(p))
                    .toList();
            assertThat(nedostaju).as(kotlin + " → " + java.getSimpleName()).isEmpty();
        });
    }

    @Test
    void provjeraHvataNedostajucePoljeNullIKrivTip() {
        Map<String, Object> dobar = new java.util.HashMap<>(Map.of(
                "brojJela", 311, "brojNamirnica", 182, "brojMenija", 84, "izvorNormativa", "NN 120/2013"));
        KotlinUgovor.provjeri(dobar, "IzvoriPodataka");

        Map<String, Object> bez = new java.util.HashMap<>(dobar);
        bez.remove("brojMenija");
        assertThatThrownBy(() -> KotlinUgovor.provjeri(bez, "IzvoriPodataka")).hasMessageContaining("brojMenija");

        Map<String, Object> sNull = new java.util.HashMap<>(dobar);
        sNull.put("izvorNormativa", null);
        assertThatThrownBy(() -> KotlinUgovor.provjeri(sNull, "IzvoriPodataka")).hasMessageContaining("nullable");

        Map<String, Object> krivTip = new java.util.HashMap<>(dobar);
        krivTip.put("brojJela", "311");
        assertThatThrownBy(() -> KotlinUgovor.provjeri(krivTip, "IzvoriPodataka")).hasMessageContaining("cijeli broj");

        assertThatThrownBy(() -> KotlinUgovor.provjeri(List.of(bez), "List<IzvoriPodataka>"))
                .hasMessageContaining("[0].brojMenija");
    }

    @Test
    void serializedNameImaPrednostPredNazivomSvojstva() {
        assertThat(KotlinUgovor.polja("Obrok")).contains("ukupnoKalorije", "stavke");
        assertThat(KotlinUgovor.klase()).containsKeys("DanPregled", "Tjedan", "TjedanDan", "PrijedlogStavka");
    }
}
