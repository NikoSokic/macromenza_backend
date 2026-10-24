package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ObrokDTO {

    private Long id;
    private LocalDate datum;
    private String vrijeme;
    private String tipObroka;
    private String izvor;
    private List<Stavka> stavke;
    private BigDecimal ukupnoKalorije;
    private BigDecimal ukupnoProteini;
    private BigDecimal ukupnoUgljikohidrati;
    private BigDecimal ukupnoMasti;

    public ObrokDTO() {
    }

    public ObrokDTO(Long id, LocalDate datum, String vrijeme, String tipObroka, String izvor, List<Stavka> stavke,
                    BigDecimal ukupnoKalorije, BigDecimal ukupnoProteini,
                    BigDecimal ukupnoUgljikohidrati, BigDecimal ukupnoMasti) {
        this.id = id;
        this.datum = datum;
        this.vrijeme = vrijeme;
        this.tipObroka = tipObroka;
        this.izvor = izvor;
        this.stavke = stavke;
        this.ukupnoKalorije = ukupnoKalorije;
        this.ukupnoProteini = ukupnoProteini;
        this.ukupnoUgljikohidrati = ukupnoUgljikohidrati;
        this.ukupnoMasti = ukupnoMasti;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public String getTipObroka() {
        return tipObroka;
    }

    public void setTipObroka(String tipObroka) {
        this.tipObroka = tipObroka;
    }

    public String getIzvor() {
        return izvor;
    }

    public void setIzvor(String izvor) {
        this.izvor = izvor;
    }

    public List<Stavka> getStavke() {
        return stavke;
    }

    public void setStavke(List<Stavka> stavke) {
        this.stavke = stavke;
    }

    public BigDecimal getUkupnoKalorije() {
        return ukupnoKalorije;
    }

    public void setUkupnoKalorije(BigDecimal ukupnoKalorije) {
        this.ukupnoKalorije = ukupnoKalorije;
    }

    public BigDecimal getUkupnoProteini() {
        return ukupnoProteini;
    }

    public void setUkupnoProteini(BigDecimal ukupnoProteini) {
        this.ukupnoProteini = ukupnoProteini;
    }

    public BigDecimal getUkupnoUgljikohidrati() {
        return ukupnoUgljikohidrati;
    }

    public void setUkupnoUgljikohidrati(BigDecimal ukupnoUgljikohidrati) {
        this.ukupnoUgljikohidrati = ukupnoUgljikohidrati;
    }

    public BigDecimal getUkupnoMasti() {
        return ukupnoMasti;
    }

    public void setUkupnoMasti(BigDecimal ukupnoMasti) {
        this.ukupnoMasti = ukupnoMasti;
    }

    public static class Stavka {

        private Long idJelo;
        private String naziv;
        private BigDecimal kolicina;
        private BigDecimal kalorije;
        private BigDecimal proteini;
        private BigDecimal ugljikohidrati;
        private BigDecimal masti;

        public Stavka() {
        }

        public Stavka(Long idJelo, String naziv, BigDecimal kolicina, BigDecimal kalorije,
                      BigDecimal proteini, BigDecimal ugljikohidrati, BigDecimal masti) {
            this.idJelo = idJelo;
            this.naziv = naziv;
            this.kolicina = kolicina;
            this.kalorije = kalorije;
            this.proteini = proteini;
            this.ugljikohidrati = ugljikohidrati;
            this.masti = masti;
        }

        public Long getIdJelo() {
            return idJelo;
        }

        public void setIdJelo(Long idJelo) {
            this.idJelo = idJelo;
        }

        public String getNaziv() {
            return naziv;
        }

        public void setNaziv(String naziv) {
            this.naziv = naziv;
        }

        public BigDecimal getKolicina() {
            return kolicina;
        }

        public void setKolicina(BigDecimal kolicina) {
            this.kolicina = kolicina;
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
    }
}
