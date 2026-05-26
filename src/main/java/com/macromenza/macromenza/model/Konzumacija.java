package com.macromenza.macromenza.model;

import jakarta.persistence.*;
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

    private LocalDate datum;
    private Integer kolicina;
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

    public LocalDate getDatum() {
        return datum;
    }

    public Integer getKolicina() {
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

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public void setKolicina(Integer kolicina) {
        this.kolicina = kolicina;
    }

    public void setTip_obroka(String tip_obroka) {
        this.tip_obroka = tip_obroka;
    }
}