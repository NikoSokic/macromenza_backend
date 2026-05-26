package com.macromenza.macromenza.dto;

import java.time.LocalDate;
import java.util.List;

public class TjedniPregledDTO {

    private LocalDate datumOd;
    private LocalDate datumDo;

    private Integer ukupnoKalorije;
    private Integer ukupnoProteini;
    private Integer ukupnoUgljikohidrati;
    private Integer ukupnoMasti;

    private Double prosjekKalorija;

    private List<DnevniPregledDTO> dani;

    public TjedniPregledDTO(
            LocalDate datumOd,
            LocalDate datumDo,
            Integer ukupnoKalorije,
            Integer ukupnoProteini,
            Integer ukupnoUgljikohidrati,
            Integer ukupnoMasti,
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

    public Integer getUkupnoKalorije() {
        return ukupnoKalorije;
    }

    public Integer getUkupnoProteini() {
        return ukupnoProteini;
    }

    public Integer getUkupnoUgljikohidrati() {
        return ukupnoUgljikohidrati;
    }

    public Integer getUkupnoMasti() {
        return ukupnoMasti;
    }

    public Double getProsjekKalorija() {
        return prosjekKalorija;
    }

    public List<DnevniPregledDTO> getDani() {
        return dani;
    }
}