package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class UkupniUnosDTO {

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public UkupniUnosDTO() {}

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