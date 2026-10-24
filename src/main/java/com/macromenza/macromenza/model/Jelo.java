package com.macromenza.macromenza.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "jelo")
public class Jelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;
    private String opis;

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    private Integer sifra;
    private String kategorija;
    private BigDecimal cijenaEur;
    private String napomena;
    private Boolean aktivno = true;

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

    public BigDecimal getKalorije() {
        return kalorije;
    }

    public BigDecimal getProteini() {
        return proteini;
    }

    public BigDecimal getUgljikohidrati() {
        return ugljikohidrati;
    }

    public BigDecimal getMasti() {
        return masti;
    }

    public Integer getSifra() {
        return sifra;
    }

    public String getKategorija() {
        return kategorija;
    }

    public BigDecimal getCijenaEur() {
        return cijenaEur;
    }

    public String getNapomena() {
        return napomena;
    }

    public Boolean getAktivno() {
        return aktivno;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public void setKalorije(BigDecimal kalorije) {
        this.kalorije = kalorije;
    }

    public void setProteini(BigDecimal proteini) {
        this.proteini = proteini;
    }

    public void setUgljikohidrati(BigDecimal ugljikohidrati) {
        this.ugljikohidrati = ugljikohidrati;
    }

    public void setMasti(BigDecimal masti) {
        this.masti = masti;
    }

    public void setSifra(Integer sifra) {
        this.sifra = sifra;
    }

    public void setKategorija(String kategorija) {
        this.kategorija = kategorija;
    }

    public void setCijenaEur(BigDecimal cijenaEur) {
        this.cijenaEur = cijenaEur;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public void setAktivno(Boolean aktivno) {
        this.aktivno = aktivno;
    }
}
