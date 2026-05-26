package com.macromenza.macromenza.dto;

import java.time.LocalDate;

public class DnevniPregledDTO {

    private LocalDate datum;
    private Integer kalorije;
    private Integer proteini;
    private Integer ugljikohidrati;
    private Integer masti;

    public DnevniPregledDTO(
            LocalDate datum,
            Integer kalorije,
            Integer proteini,
            Integer ugljikohidrati,
            Integer masti
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