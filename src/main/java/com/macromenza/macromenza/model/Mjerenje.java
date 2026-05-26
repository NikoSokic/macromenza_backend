package com.macromenza.macromenza.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mjerenje")
public class Mjerenje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_korisnik")
    private Long idKorisnik;

    private Integer masa;
    private String razina_aktivnosti;
    private String tip_cilja;
    private LocalDate datum;

    public Mjerenje() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public Integer getMasa() {
        return masa;
    }

    public String getRazina_aktivnosti() {
        return razina_aktivnosti;
    }

    public String getTip_cilja() {
        return tip_cilja;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public void setMasa(Integer masa) {
        this.masa = masa;
    }

    public void setRazina_aktivnosti(String razina_aktivnosti) {
        this.razina_aktivnosti = razina_aktivnosti;
    }

    public void setTip_cilja(String tip_cilja) {
        this.tip_cilja = tip_cilja;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }
}