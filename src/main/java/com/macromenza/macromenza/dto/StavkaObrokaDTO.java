package com.macromenza.macromenza.dto;

public class StavkaObrokaDTO {

    private String nazivJela;
    private String tipObroka;

    private Integer kolicina;

    private Integer kalorije;
    private Integer proteini;
    private Integer ugljikohidrati;
    private Integer masti;

    public StavkaObrokaDTO(
            String nazivJela,
            String tipObroka,
            Integer kolicina,
            Integer kalorije,
            Integer proteini,
            Integer ugljikohidrati,
            Integer masti
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

    public Integer getKalorije() {
        return kalorije;
    }

    public Integer getProteini() {
        return proteini;
    }

    public Integer getUgljikohidrati() {
        return ugljikohidrati;
    }

    public Integer getMasti() {
        return masti;
    }
}