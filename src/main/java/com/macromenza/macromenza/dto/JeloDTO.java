package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class JeloDTO {

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

    public JeloDTO() {
    }

    public JeloDTO(Long id, String naziv, String opis, BigDecimal kalorije, BigDecimal proteini,
                   BigDecimal ugljikohidrati, BigDecimal masti, Integer sifra, String kategorija,
                   BigDecimal cijenaEur, String napomena) {
        this.id = id;
        this.naziv = naziv;
        this.opis = opis;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
        this.sifra = sifra;
        this.kategorija = kategorija;
        this.cijenaEur = cijenaEur;
        this.napomena = napomena;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public BigDecimal getKalorije() {
        return kalorije;
    }

    public void setKalorije(BigDecimal kalorije) {
        this.kalorije = kalorije;
    }

    public BigDecimal getProteini() {
        return proteini;
    }

    public void setProteini(BigDecimal proteini) {
        this.proteini = proteini;
    }

    public BigDecimal getUgljikohidrati() {
        return ugljikohidrati;
    }

    public void setUgljikohidrati(BigDecimal ugljikohidrati) {
        this.ugljikohidrati = ugljikohidrati;
    }

    public BigDecimal getMasti() {
        return masti;
    }

    public void setMasti(BigDecimal masti) {
        this.masti = masti;
    }

    public Integer getSifra() {
        return sifra;
    }

    public void setSifra(Integer sifra) {
        this.sifra = sifra;
    }

    public String getKategorija() {
        return kategorija;
    }

    public void setKategorija(String kategorija) {
        this.kategorija = kategorija;
    }

    public BigDecimal getCijenaEur() {
        return cijenaEur;
    }

    public void setCijenaEur(BigDecimal cijenaEur) {
        this.cijenaEur = cijenaEur;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
}
