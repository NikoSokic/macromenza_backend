package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class JeloSastojakDTO {

    private String nazivURecepturi;
    private String namirnicaKljuc;
    private BigDecimal grami;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public JeloSastojakDTO() {
    }

    public JeloSastojakDTO(String nazivURecepturi, String namirnicaKljuc, BigDecimal grami, BigDecimal kalorije,
                           BigDecimal proteini, BigDecimal ugljikohidrati, BigDecimal masti) {
        this.nazivURecepturi = nazivURecepturi;
        this.namirnicaKljuc = namirnicaKljuc;
        this.grami = grami;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
    }

    public String getNazivURecepturi() {
        return nazivURecepturi;
    }

    public void setNazivURecepturi(String nazivURecepturi) {
        this.nazivURecepturi = nazivURecepturi;
    }

    public String getNamirnicaKljuc() {
        return namirnicaKljuc;
    }

    public void setNamirnicaKljuc(String namirnicaKljuc) {
        this.namirnicaKljuc = namirnicaKljuc;
    }

    public BigDecimal getGrami() {
        return grami;
    }

    public void setGrami(BigDecimal grami) {
        this.grami = grami;
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
