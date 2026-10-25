package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.util.List;

/** Jelo s masom porcije i sastojcima iz recepture; stara jela bez recepture imaju praznu listu sastojaka. */
public class JeloDetaljDTO extends JeloDTO {

    private BigDecimal masaG;
    private List<JeloSastojakDTO> sastojci;

    public JeloDetaljDTO() {
    }

    public JeloDetaljDTO(Long id, String naziv, String opis, BigDecimal kalorije, BigDecimal proteini,
                         BigDecimal ugljikohidrati, BigDecimal masti, Integer sifra, String kategorija,
                         BigDecimal cijenaEur, String napomena, BigDecimal masaG, List<JeloSastojakDTO> sastojci) {
        super(id, naziv, opis, kalorije, proteini, ugljikohidrati, masti, sifra, kategorija, cijenaEur, napomena);
        this.masaG = masaG;
        this.sastojci = sastojci;
    }

    public BigDecimal getMasaG() {
        return masaG;
    }

    public void setMasaG(BigDecimal masaG) {
        this.masaG = masaG;
    }

    public List<JeloSastojakDTO> getSastojci() {
        return sastojci;
    }

    public void setSastojci(List<JeloSastojakDTO> sastojci) {
        this.sastojci = sastojci;
    }
}
