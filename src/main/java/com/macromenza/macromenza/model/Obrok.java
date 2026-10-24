package com.macromenza.macromenza.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "obrok")
public class Obrok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_korisnik")
    private Long idKorisnik;

    private LocalDate datum;
    private LocalTime vrijeme;

    @Column(name = "tip_obroka")
    private String tipObroka;

    private String izvor;

    public Obrok() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public LocalTime getVrijeme() {
        return vrijeme;
    }

    public String getTipObroka() {
        return tipObroka;
    }

    public String getIzvor() {
        return izvor;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public void setVrijeme(LocalTime vrijeme) {
        this.vrijeme = vrijeme;
    }

    public void setTipObroka(String tipObroka) {
        this.tipObroka = tipObroka;
    }

    public void setIzvor(String izvor) {
        this.izvor = izvor;
    }
}
