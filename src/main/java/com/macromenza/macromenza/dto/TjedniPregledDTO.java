package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

public class TjedniPregledDTO {

    private LocalDate datumOd;
    private LocalDate datumDo;

    private BigDecimal ukupnoKalorije;
    private BigDecimal ukupnoProteini;
    private BigDecimal ukupnoUgljikohidrati;
    private BigDecimal ukupnoMasti;

    private Double prosjekKalorija;

    private List<DnevniPregledDTO> dani;

    public TjedniPregledDTO(
            LocalDate datumOd,
            LocalDate datumDo,
            BigDecimal ukupnoKalorije,
            BigDecimal ukupnoProteini,
            BigDecimal ukupnoUgljikohidrati,
            BigDecimal ukupnoMasti,
            Double prosjekKalorija,
            List<DnevniPregledDTO> dani
    ) {
        this.datumOd = datumOd;
        this.datumDo = datumDo;
        this.ukupnoKalorije = ukupnoKalorije;
        this.ukupnoProteini = ukupnoProteini;
        this.ukupnoUgljikohidrati = ukupnoUgljikohidrati;
        this.ukupnoMasti = ukupnoMasti;
        this.prosjekKalorija = prosjekKalorija;
        this.dani = dani;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }

    public BigDecimal getUkupnoKalorije() {
        return ukupnoKalorije;
    }

    public BigDecimal getUkupnoProteini() {
        return ukupnoProteini;
    }

    public BigDecimal getUkupnoUgljikohidrati() {
        return ukupnoUgljikohidrati;
    }

    public BigDecimal getUkupnoMasti() {
        return ukupnoMasti;
    }

    public Double getProsjekKalorija() {
        return prosjekKalorija;
    }

    public List<DnevniPregledDTO> getDani() {
        return dani;
    }
}