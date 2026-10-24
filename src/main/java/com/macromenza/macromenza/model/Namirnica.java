package com.macromenza.macromenza.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "namirnica")
public class Namirnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kljuc;

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    private BigDecimal gramaPoKomadu;
    private String napomena;

    public Namirnica() {
    }

    public Long getId() {
        return id;
    }

    public String getKljuc() {
        return kljuc;
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

    public BigDecimal getGramaPoKomadu() {
        return gramaPoKomadu;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setKljuc(String kljuc) {
        this.kljuc = kljuc;
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

    public void setGramaPoKomadu(BigDecimal gramaPoKomadu) {
        this.gramaPoKomadu = gramaPoKomadu;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
}
