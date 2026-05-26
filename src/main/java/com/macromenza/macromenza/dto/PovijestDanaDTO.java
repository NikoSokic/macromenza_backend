package com.macromenza.macromenza.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PovijestDanaDTO {

    private LocalDate datum;

    private Integer ukupnoKalorije;
    private Integer ukupnoProteini;
    private Integer ukupnoUgljikohidrati;
    private Integer ukupnoMasti;

    private Map<String, List<StavkaObrokaDTO>> obroci;

    public PovijestDanaDTO(
            LocalDate datum,
            Integer ukupnoKalorije,
            Integer ukupnoProteini,
            Integer ukupnoUgljikohidrati,
            Integer ukupnoMasti,
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

    public Map<String, List<StavkaObrokaDTO>> getObroci() {
        return obroci;
    }
}