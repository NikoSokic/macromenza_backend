package com.macromenza.macromenza.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jelo")
public class Jelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;
    private String opis;

    private Integer kalorije;
    private Integer proteini;
    private Integer ugljikohidrati;
    private Integer masti;

    public Jelo() {
    }

    public Long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getOpis() {
        return opis;
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

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public void setOpis(String opis) {
        this.opis = opis;
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


}

