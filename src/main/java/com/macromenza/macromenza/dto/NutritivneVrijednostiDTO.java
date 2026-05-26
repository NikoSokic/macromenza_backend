package com.macromenza.macromenza.dto;

public class NutritivneVrijednostiDTO {

    private Integer kalorije;
    private Integer proteini;
    private Integer ugljikohidrati;
    private Integer masti;

    public NutritivneVrijednostiDTO() {
    }

    public NutritivneVrijednostiDTO(Integer kalorije, Integer proteini, Integer ugljikohidrati, Integer masti) {
        this.kalorije = kalorije;
        this.proteini = proteini;
        this.ugljikohidrati = ugljikohidrati;
        this.masti = masti;
    }

    public Integer getKalorije() {
        return kalorije;
    }

    public void setKalorije(Integer kalorije) {
        this.kalorije = kalorije;
    }

    public Integer getProteini() {
        return proteini;
    }

    public void setProteini(Integer proteini) {
        this.proteini = proteini;
    }

    public Integer getUgljikohidrati() {
        return ugljikohidrati;
    }

    public void setUgljikohidrati(Integer ugljikohidrati) {
        this.ugljikohidrati = ugljikohidrati;
    }

    public Integer getMasti() {
        return masti;
    }

    public void setMasti(Integer masti) {
        this.masti = masti;
    }
}