package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Registracija i onboarding, korisnik, profil, stara mjerenja i preporuke koje aplikacija još koristi. */
class KorisnikControllerIT extends IntegracijskiTest {

    @Test
    void registracijaStvaraKorisnikaProfilMjerenjeIPreporuku() throws Exception {
        long id = noviKorisnik();

        Odgovor korisnik = dohvati("/korisnici/{id}", id).ocekuj(200);
        KotlinUgovor.provjeri(korisnik.json(), "Korisnik");
        assertThat(korisnik.<String>put("$.ime")).isEqualTo("Test");

        Odgovor profil = dohvati("/profil/korisnik/{id}", id).ocekuj(200);
        KotlinUgovor.provjeri(profil.json(), "ProfilPodaci");
        assertThat(profil.<Integer>put("$.visina")).isEqualTo(180);

        Odgovor mjerenja = dohvati("/mjerenja/korisnik/{id}", id).ocekuj(200);
        KotlinUgovor.provjeri(mjerenja.json(), "List<Mjerenje>");
        assertThat(mjerenja.lista()).hasSize(1);
        assertThat(broj(mjerenja.put("$[0].masa"))).isEqualTo(80.0);

        Odgovor preporuke = dohvati("/preporuke/korisnik/{id}", id).ocekuj(200);
        KotlinUgovor.provjeri(preporuke.json(), "List<Preporuka>");
        assertThat(preporuke.<List<Integer>>put("$[*].kalorije")).containsExactly(2798);

        Odgovor status = dohvati("/korisnici/{id}/onboarding-status", id).ocekuj(200);
        KotlinUgovor.provjeri(status.json(), "OnboardingStatusResponse");
        assertThat(status.<Boolean>put("$.onboardingZavrsen")).isTrue();
    }

    @Test
    void ponovnaRegistracijaIstogUidaNeStvaraDuplikat() throws Exception {
        String uid = UUID.randomUUID().toString();
        kao(UUID.fromString(uid));
        String zahtjev = """
                {"supabaseUid":"%s","email":"dup@macromenza.test","ime":"Ana","prezime":"Anić","spol":"Z",
                 "visina":165,"dob":30,"masa":60.0,"razinaAktivnosti":"sjedilacka","tipCilja":"mrsavljenje"}""";

        Number prvi = objavi("/korisnici/registracija-profil", zahtjev.formatted(uid)).ocekuj(200).put("$.id");
        Number drugi = objavi("/korisnici/registracija-profil", zahtjev.formatted(uid).replace("Ana", "Ivana"))
                .ocekuj(200).put("$.id");
        assertThat(drugi.longValue()).isEqualTo(prvi.longValue());
        assertThat(dohvati("/korisnici/{id}", prvi).<String>put("$.ime")).isEqualTo("Ivana");
        // Ž, 30, 165, 60, sjedilačka, mršavljenje = 1084 kcal
        assertThat(dohvati("/api/preporuke/{id}", prvi).<Integer>put("$.kalorije")).isEqualTo(1084);

        Odgovor supabase = objavi("/korisnici/supabase", """
                {"supabaseUid":"%s","ime":"X","prezime":"Y","email":"x@y.z"}""".formatted(uid))
                .ocekuj(200);
        KotlinUgovor.provjeri(supabase.json(), "Korisnik");
        assertThat(supabase.<Number>put("$.id").longValue()).isEqualTo(prvi.longValue());
        assertThat(supabase.<String>put("$.ime")).isEqualTo("Ivana"); // postojeći se ne prepisuje
    }

    @Test
    void urediProfilIKorisnika() throws Exception {
        long id = noviKorisnik();

        Odgovor k = zamijeni("/korisnici/{id}", """
                {"ime":"Marko","prezime":"Marić","email":"marko@macromenza.test"}""", id)
                .ocekuj(200);
        KotlinUgovor.provjeri(k.json(), "Korisnik");
        assertThat(k.<String>put("$.prezime")).isEqualTo("Marić");

        Number idProfila = dohvati("/profil/korisnik/{id}", id).put("$.id");
        Odgovor p = zamijeni("/profil/{id}", """
                {"idKorisnik":%d,"visina":182,"spol":"M","dob":26}""".formatted(id), idProfila).ocekuj(200);
        KotlinUgovor.provjeri(p.json(), "ProfilPodaci");
        assertThat(p.<Integer>put("$.visina")).isEqualTo(182);

        // Postavke ciljeva: novo mjerenje pa izračun (kao PostavkeCiljevaScreen)
        Odgovor m = objavi("/mjerenja", """
                {"idKorisnik":%d,"masa":78.5,"razina_aktivnosti":"visoka","tip_cilja":"dobivanje_mase","datum":"%s"}"""
                .formatted(id, DANAS)).ocekuj(200);
        KotlinUgovor.provjeri(m.json(), "Mjerenje");

        Odgovor pr = objavi("/preporuke/izracunaj/{id}", "", id).ocekuj(200);
        KotlinUgovor.provjeri(pr.json(), "Preporuka");
        // 10·78,5 + 6,25·182 − 5·26 + 5 = 1797,5; ×1,725 = 3100,7; +300 = 3400,7
        assertThat(pr.<Integer>put("$.kalorije")).isEqualTo(3401);
        assertThat(dohvati("/api/preporuke/{id}", id).<Integer>put("$.kalorije")).isEqualTo(3401);
    }

    @Test
    void onboardingNijeZavrsenBezMjerenjaIliZaNepostojecegKorisnika() throws Exception {
        // Tuđi ili nepostojeći id: korisnik iz tokena nije vlasnik, pa je odgovor 403.
        dohvati("/korisnici/{id}/onboarding-status", -1).ocekuj(403);

        UUID uid = UUID.randomUUID();
        kao(uid);
        Number id = objavi("/korisnici/supabase", """
                {"supabaseUid":"%s","ime":"EMPTY","prezime":"EMPTY","email":"novi@macromenza.test"}"""
                .formatted(uid)).ocekuj(200).put("$.id");
        assertThat(dohvati("/korisnici/{id}/onboarding-status", id).<Boolean>put("$.onboardingZavrsen")).isFalse();
    }
}
