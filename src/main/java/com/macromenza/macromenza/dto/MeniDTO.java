package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.util.List;

public class MeniDTO {

    private Long id;
    private String sifra;
    private String tip;
    private Boolean vegetarijanski;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;
    private List<MeniStavkaDTO> stavke;

    public MeniDTO() {
    }

    public MeniDTO(Long id, String sifra, String tip, Boolean vegetarijanski,
                   BigDecimal kalorije, BigDecimal proteini, BigDecimal ugljikohidrati, BigDecimal masti,
                   List<MeniStavkaDTO> stavke) {
        this.id = id;
        this.sifra = sifra;
        this.tip = tip;
        this.vegetarijanski = vegetarijanski;
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
        this.stavke = stavke;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSifra() {
        return sifra;
    }

    public void setSifra(String sifra) {
        this.sifra = sifra;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public Boolean getVegetarijanski() {
        return vegetarijanski;
    }

    public void setVegetarijanski(Boolean vegetarijanski) {
        this.vegetarijanski = vegetarijanski;
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

    public List<MeniStavkaDTO> getStavke() {
        return stavke;
    }

    public void setStavke(List<MeniStavkaDTO> stavke) {
        this.stavke = stavke;
    }
}
