package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class MeniStavkaDTO {

    private Long idJelo;
    private String oznaka;
    private String nazivJela;
    private BigDecimal kolicina;
    private Boolean priblizno;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public MeniStavkaDTO() {
    }

    public MeniStavkaDTO(Long idJelo, String oznaka, String nazivJela, BigDecimal kolicina, Boolean priblizno,
                         BigDecimal kalorije, BigDecimal proteini, BigDecimal ugljikohidrati, BigDecimal masti) {
        this.idJelo = idJelo;
        this.oznaka = oznaka;
        this.nazivJela = nazivJela;
        this.kolicina = kolicina;
        this.priblizno = priblizno;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
    }

    public Long getIdJelo() {
        return idJelo;
    }

    public void setIdJelo(Long idJelo) {
        this.idJelo = idJelo;
    }

    public String getOznaka() {
        return oznaka;
    }

    public void setOznaka(String oznaka) {
        this.oznaka = oznaka;
    }

    public String getNazivJela() {
        return nazivJela;
    }

    public void setNazivJela(String nazivJela) {
        this.nazivJela = nazivJela;
    }

    public BigDecimal getKolicina() {
        return kolicina;
    }

    public void setKolicina(BigDecimal kolicina) {
        this.kolicina = kolicina;
    }

    public Boolean getPriblizno() {
        return priblizno;
    }

    public void setPriblizno(Boolean priblizno) {
        this.priblizno = priblizno;
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
}
