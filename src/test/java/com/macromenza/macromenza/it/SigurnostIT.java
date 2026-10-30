package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Korak 19/20: token je obavezan, korisnik vidi samo svoje podatke, a greške imaju ispravne HTTP statuse. */
class SigurnostIT extends IntegracijskiTest {

    @Test
    void pingJeOtvorenAliSveOstaloTraziToken() throws Exception {
        kao(null);

        dohvati("/api/ping").ocekuj(200);

        Odgovor bezTokena = dohvati("/api/jela").ocekuj(401);
        assertThat(bezTokena.poruka()).contains("Prijava");
        dohvati("/api/obroci?idKorisnik=1&datum=2026-01-01").ocekuj(401);
        dohvati("/korisnici").ocekuj(401);
        objavi("/api/prepoznavanje", "").ocekuj(401);
    }

    @Test
    void korisnikNeVidiTudePodatke() throws Exception {
        long moj = noviKorisnik();
        UUID mojUid = trenutniUid;
        long tudi = noviKorisnik();

        kao(mojUid);
        dohvati("/korisnici/{id}", moj).ocekuj(200);
        dohvati("/korisnici/{id}", tudi).ocekuj(403);
        zamijeni("/korisnici/{id}", "{\"ime\":\"X\",\"prezime\":\"Y\",\"email\":\"x@y.z\"}", tudi).ocekuj(403);
        dohvati("/korisnici/{id}/onboarding-status", tudi).ocekuj(403);
        dohvati("/profil/korisnik/{id}", tudi).ocekuj(403);
        dohvati("/mjerenja/korisnik/{id}", tudi).ocekuj(403);
        dohvati("/preporuke/korisnik/{id}", tudi).ocekuj(403);
        objavi("/preporuke/izracunaj/{id}", "", tudi).ocekuj(403);
        dohvati("/api/jela/cesto?idKorisnik={k}", tudi).ocekuj(403);
        dohvati("/api/obroci?idKorisnik={k}&datum={d}", tudi, DANAS).ocekuj(403);
        dohvati("/api/obroci/dan-pregled?idKorisnik={k}", tudi).ocekuj(403);
        dohvati("/api/obroci/tjedan?idKorisnik={k}", tudi).ocekuj(403);
        dohvati("/api/obroci/1?idKorisnik={k}", tudi).ocekuj(403);
        obrisi("/api/obroci/1?idKorisnik={k}", tudi).ocekuj(403);
        objavi("/api/obroci/primjer-tjedna?idKorisnik={k}", "", tudi).ocekuj(403);
        obrisi("/api/obroci/primjer-tjedna?idKorisnik={k}", tudi).ocekuj(403);
        dohvati("/api/mjerenja?idKorisnik={k}", tudi).ocekuj(403);
        dohvati("/api/konzumacije/povijest/{k}?datum={d}", tudi, DANAS).ocekuj(403);
        obrisi("/api/korisnici/{id}/podaci?potvrda=OBRISI", tudi).ocekuj(403);

        objavi("/api/obroci", obrokJson(tudi, DANAS, "RUCAK", stavka(aktivnaJela(1).get(0).get("id"), "1"))).ocekuj(403);
        objavi("/api/mjerenja/brzo", "{\"idKorisnik\":%d,\"masa\":80.0}".formatted(tudi)).ocekuj(403);
        objavi("/mjerenja", "{\"idKorisnik\":%d,\"masa\":80.0,\"datum\":\"%s\"}".formatted(tudi, DANAS)).ocekuj(403);
    }

    @Test
    void tudiProfilSeNeMozeUrediti() throws Exception {
        long tudi = noviKorisnik();
        Number idTudegProfila = dohvati("/profil/korisnik/{id}", tudi).put("$.id");

        noviKorisnik();
        zamijeni("/profil/{id}", "{\"idKorisnik\":%d,\"visina\":150,\"spol\":\"M\",\"dob\":20}".formatted(tudi),
                idTudegProfila).ocekuj(403);
    }

    @Test
    void uidIzTijelaMoraBitiUidIzTokena() throws Exception {
        UUID uid = UUID.randomUUID();
        kao(uid);
        String tudiUid = UUID.randomUUID().toString();

        objavi("/korisnici/supabase", "{\"supabaseUid\":\"%s\",\"ime\":\"X\",\"prezime\":\"Y\",\"email\":\"x@y.z\"}"
                .formatted(tudiUid)).ocekuj(403);
        objavi("/korisnici/registracija-profil", """
                {"supabaseUid":"%s","email":"a@b.c","ime":"A","prezime":"B","spol":"M","visina":180,"dob":25,
                 "masa":80.0,"razinaAktivnosti":"umjerena","tipCilja":"odrzavanje"}""".formatted(tudiUid)).ocekuj(403);

        // Bez uida u tijelu uzima se uid iz tokena.
        Number id = objavi("/korisnici/supabase", "{\"ime\":\"X\",\"prezime\":\"Y\",\"email\":\"x@y.z\"}")
                .ocekuj(200).put("$.id");
        assertThat(dohvati("/korisnici/{id}", id).<String>put("$.supabaseUid")).isEqualTo(uid.toString());
    }

    @Test
    void odgovorKorisnikaNemaLozinku() throws Exception {
        long id = noviKorisnik();
        assertThat(dohvati("/korisnici/{id}", id).ocekuj(200).tijelo()).doesNotContain("lozinka");
    }

    @Test
    void opasniEndpointiViseNePostoje() throws Exception {
        long id = noviKorisnik();

        for (String url : new String[]{"/korisnici", "/profil", "/mjerenja", "/preporuke", "/api/konzumacije"}) {
            zatvoren(dohvati(url), "GET " + url);
        }
        zatvoren(objavi("/api/jela", "{}"), "POST /api/jela");
        zatvoren(zamijeni("/api/jela/1", "{}"), "PUT /api/jela/1");
        zatvoren(obrisi("/api/jela/1"), "DELETE /api/jela/1");
        zatvoren(obrisi("/korisnici/{id}", id), "DELETE /korisnici/id");
        zatvoren(obrisi("/profil/{id}", id), "DELETE /profil/id");
        zatvoren(obrisi("/mjerenja/{id}", id), "DELETE /mjerenja/id");
        zatvoren(obrisi("/preporuke/{id}", id), "DELETE /preporuke/id");
        zatvoren(obrisi("/api/konzumacije/{id}", id), "DELETE /api/konzumacije/id");
        zatvoren(objavi("/api/konzumacije", "{}"), "POST /api/konzumacije");
        zatvoren(dohvati("/api/konzumacije/korisnik/{id}", id), "GET /api/konzumacije/korisnik/id");
        zatvoren(dohvati("/korisnici/supabase/{uid}", UUID.randomUUID()), "GET /korisnici/supabase/uid");
    }

    private static void zatvoren(Odgovor odgovor, String zahtjev) {
        assertThat(odgovor.status()).as(zahtjev).isIn(404, 405);
    }

    @Test
    void greskeImajuIspravneStatuse() throws Exception {
        long id = noviKorisnik();

        dohvati("/api/jela/{id}/detalj", 999999).ocekuj(404);
        dohvati("/nema-takvog-puta").ocekuj(404);
        dohvati("/api/obroci?idKorisnik={k}", id).ocekuj(400);
        dohvati("/api/obroci?idKorisnik=abc&datum=2026-01-01").ocekuj(400);
        dohvati("/api/obroci/{id}?idKorisnik={k}", 999999, id).ocekuj(404);
        assertThat(dohvati("/api/obroci?idKorisnik={k}", id).poruka()).contains("datum");
    }
}
