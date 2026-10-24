package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class StavkaObrokaDTO {

    private String nazivJela;
    private String tipObroka;

    private Integer kolicina;

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public StavkaObrokaDTO(
            String nazivJela,
            String tipObroka,
            Integer kolicina,
            BigDecimal kalorije,
            BigDecimal proteini,
            BigDecimal ugljikohidrati,
            BigDecimal masti
    ) {
        this.nazivJela = nazivJela;
        this.tipObroka = tipObroka;
        this.kolicina = kolicina;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
    }

    public String getNazivJela() {
        return nazivJela;
    }

    public String getTipObroka() {
        return tipObroka;
    }

    public Integer getKolicina() {
        return kolicina;
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
}