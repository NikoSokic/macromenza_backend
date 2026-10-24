package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class NamirnicaDTO {

    private Long id;
    private String kljuc;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;
    private BigDecimal gramaPoKomadu;
    private String napomena;

    public NamirnicaDTO() {
    }

    public NamirnicaDTO(Long id, String kljuc, BigDecimal kalorije, BigDecimal proteini,
                        BigDecimal ugljikohidrati, BigDecimal masti,
                        BigDecimal gramaPoKomadu, String napomena) {
        this.id = id;
        this.kljuc = kljuc;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
        this.gramaPoKomadu = gramaPoKomadu;
        this.napomena = napomena;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKljuc() {
        return kljuc;
    }

    public void setKljuc(String kljuc) {
        this.kljuc = kljuc;
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

    public BigDecimal getGramaPoKomadu() {
        return gramaPoKomadu;
    }

    public void setGramaPoKomadu(BigDecimal gramaPoKomadu) {
        this.gramaPoKomadu = gramaPoKomadu;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
}
