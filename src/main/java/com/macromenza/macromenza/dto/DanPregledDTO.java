package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.util.List;

public class DanPregledDTO {

    private NutritivneVrijednostiDTO cilj;
    private NutritivneVrijednostiDTO pojedeno;
    private List<PoTipu> poTipu;
    private Savjet savjet;

    public DanPregledDTO() {
    }

    public DanPregledDTO(
            NutritivneVrijednostiDTO cilj,
            NutritivneVrijednostiDTO pojedeno,
            List<PoTipu> poTipu,
            Savjet savjet
    ) {
        this.cilj = cilj;
        this.pojedeno = pojedeno;
        this.poTipu = poTipu;
        this.savjet = savjet;
    }

    public NutritivneVrijednostiDTO getCilj() {
        return cilj;
    }

    public void setCilj(NutritivneVrijednostiDTO cilj) {
        this.cilj = cilj;
    }

    public NutritivneVrijednostiDTO getPojedeno() {
        return pojedeno;
    }

    public void setPojedeno(NutritivneVrijednostiDTO pojedeno) {
        this.pojedeno = pojedeno;
    }

    public List<PoTipu> getPoTipu() {
        return poTipu;
    }

    public void setPoTipu(List<PoTipu> poTipu) {
        this.poTipu = poTipu;
    }

    public Savjet getSavjet() {
        return savjet;
    }

    public void setSavjet(Savjet savjet) {
        this.savjet = savjet;
    }

    /** Kalorije i broj obroka jednog tipa obroka u danu. */
    public static class PoTipu {

        private String tipObroka;
        private BigDecimal kalorije;
        private long brojObroka;

        public PoTipu() {
        }

        public PoTipu(String tipObroka, BigDecimal kalorije, long brojObroka) {
            this.tipObroka = tipObroka;
            this.kalorije = kalorije;
            this.brojObroka = brojObroka;
        }

        public String getTipObroka() {
            return tipObroka;
        }

        public void setTipObroka(String tipObroka) {
            this.tipObroka = tipObroka;
        }

        public BigDecimal getKalorije() {
            return kalorije;
        }

        public void setKalorije(BigDecimal kalorije) {
            this.kalorije = kalorije;
        }

        public long getBrojObroka() {
            return brojObroka;
        }

        public void setBrojObroka(long brojObroka) {
            this.brojObroka = brojObroka;
        }
    }

    public static class Savjet {

        private String naslov;
        private String tekst;
        private List<SavjetJelo> jela;

        public Savjet() {
        }

        public Savjet(String naslov, String tekst, List<SavjetJelo> jela) {
            this.naslov = naslov;
            this.tekst = tekst;
            this.jela = jela;
        }

        public String getNaslov() {
            return naslov;
        }

        public void setNaslov(String naslov) {
            this.naslov = naslov;
        }

        public String getTekst() {
            return tekst;
        }

        public void setTekst(String tekst) {
            this.tekst = tekst;
        }

        public List<SavjetJelo> getJela() {
            return jela;
        }

        public void setJela(List<SavjetJelo> jela) {
            this.jela = jela;
        }
    }

    public static class SavjetJelo {

        private Long idJelo;
        private String naziv;
        private BigDecimal kalorije;
        private BigDecimal proteini;

        public SavjetJelo() {
        }

        public SavjetJelo(Long idJelo, String naziv, BigDecimal kalorije, BigDecimal proteini) {
            this.idJelo = idJelo;
            this.naziv = naziv;
            this.kalorije = kalorije;
            this.proteini = proteini;
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
    }
}
