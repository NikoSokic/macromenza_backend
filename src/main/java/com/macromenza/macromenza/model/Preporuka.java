package com.macromenza.macromenza.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "preporuka")
public class Preporuka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_korisnik")
    private Long idKorisnik;

    private Integer kalorije;
    private Integer proteini;
    private Integer ugljikohidrati;
    private Integer masti;
    private LocalDate datum;

    public Preporuka() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public Integer getKalorije() {
        return kalorije;
    }

    public Integer getProteini() {
        return proteini;
    }

    public Integer getUgljikohidrati() {
        return ugljikohidrati;
    }

    public Integer getMasti() {
        return masti;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public void setKalorije(Integer kalorije) {
        this.kalorije = kalorije;
    }

    public void setProteini(Integer proteini) {
        this.proteini = proteini;
    }

    public void setUgljikohidrati(Integer ugljikohidrati) {
        this.ugljikohidrati = ugljikohidrati;
    }

    public void setMasti(Integer masti) {
        this.masti = masti;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }
}