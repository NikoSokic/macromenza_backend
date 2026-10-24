package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

import java.time.LocalDate;

public class DnevniPregledDTO {

    private LocalDate datum;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public DnevniPregledDTO(
            LocalDate datum,
            BigDecimal kalorije,
            BigDecimal proteini,
            BigDecimal ugljikohidrati,
            BigDecimal masti
    ) {
        this.datum = datum;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
    }

    public LocalDate getDatum() {
        return datum;
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