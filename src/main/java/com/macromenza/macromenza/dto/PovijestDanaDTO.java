package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PovijestDanaDTO {

    private LocalDate datum;

    private BigDecimal ukupnoKalorije;
    private BigDecimal ukupnoProteini;
    private BigDecimal ukupnoUgljikohidrati;
    private BigDecimal ukupnoMasti;

    private Map<String, List<StavkaObrokaDTO>> obroci;

    public PovijestDanaDTO(
            LocalDate datum,
            BigDecimal ukupnoKalorije,
            BigDecimal ukupnoProteini,
            BigDecimal ukupnoUgljikohidrati,
            BigDecimal ukupnoMasti,
            Map<String, List<StavkaObrokaDTO>> obroci
    ) {
        this.datum = datum;
        this.ukupnoKalorije = ukupnoKalorije;
        this.ukupnoProteini = ukupnoProteini;
        this.ukupnoUgljikohidrati = ukupnoUgljikohidrati;
        this.ukupnoMasti = ukupnoMasti;
        this.obroci = obroci;
    }

    public LocalDate getDatum() {
        return datum;
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

    public Map<String, List<StavkaObrokaDTO>> getObroci() {
        return obroci;
    }
}