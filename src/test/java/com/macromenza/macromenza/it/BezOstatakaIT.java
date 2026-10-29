package com.macromenza.macromenza.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Izvan transakcije: u bazi ne smije ostati nijedan testni korisnik (email @macromenza.test) ni njegovi podaci.
 * Ako rollback ikad prestane raditi, ovaj test pada. Ispisuje i broj redova glavnih tablica.
 */
@SpringBootTest
class BezOstatakaIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void testniPodaciNeOstajuUBazi() {
        String testni = "SELECT id FROM korisnik WHERE email LIKE '%@macromenza.test'";

        assertThat(broj("SELECT count(*) FROM korisnik WHERE email LIKE '%@macromenza.test'")).isZero();
        assertThat(broj("SELECT count(*) FROM obrok WHERE id_korisnik IN (" + testni + ")")).isZero();
        assertThat(broj("SELECT count(*) FROM mjerenje WHERE id_korisnik IN (" + testni + ")")).isZero();

        System.out.printf("STANJE BAZE: korisnik=%d obrok=%d konzumacija=%d mjerenje=%d preporuka=%d profil_podaci=%d%n",
                broj("SELECT count(*) FROM korisnik"), broj("SELECT count(*) FROM obrok"),
                broj("SELECT count(*) FROM konzumacija"), broj("SELECT count(*) FROM mjerenje"),
                broj("SELECT count(*) FROM preporuka"), broj("SELECT count(*) FROM profil_podaci"));
    }

    private long broj(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }
}
