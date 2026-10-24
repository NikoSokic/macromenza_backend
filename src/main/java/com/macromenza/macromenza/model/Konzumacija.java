package com.macromenza.macromenza.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "konzumacija")
public class Konzumacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_korisnik")
    private Long idKorisnik;

    @Column(name = "id_jelo")
    private Long idJelo;

    @Column(name = "id_obrok")
    private Long idObrok;

    private LocalDate datum;
    private BigDecimal kolicina;
    private String tip_obroka;

    public Konzumacija() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public Long getIdJelo() {
        return idJelo;
    }

    public Long getIdObrok() {
        return idObrok;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public BigDecimal getKolicina() {
        return kolicina;
    }

    public String getTip_obroka() {
        return tip_obroka;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public void setIdJelo(Long idJelo) {
        this.idJelo = idJelo;
    }

    public void setIdObrok(Long idObrok) {
        this.idObrok = idObrok;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public void setKolicina(BigDecimal kolicina) {
        this.kolicina = kolicina;
    }

    public void setTip_obroka(String tip_obroka) {
        this.tip_obroka = tip_obroka;
    }
}
