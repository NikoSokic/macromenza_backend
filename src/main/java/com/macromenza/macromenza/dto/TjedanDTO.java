package com.macromenza.macromenza.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tjedan pon–ned za Povijest: točno 7 dana. [prosjek] i [prosjekCilj] računaju se samo preko dana s unosom
 * i null su kad takvih dana nema ([prosjekCilj] i kad nijedan dan s unosom nema cilj).
 */
public class TjedanDTO {

    private LocalDate pocetak;
    private LocalDate kraj;
    private List<Dan> dani;
    private int daniSUnosom;
    private int daniUCilju;
    private NutritivneVrijednostiDTO prosjek;
    private NutritivneVrijednostiDTO prosjekCilj;
    private boolean imaPrethodni;
    private boolean imaSljedeci;
    private boolean imaPrimjer;

    public TjedanDTO() {
    }

    public TjedanDTO(
            LocalDate pocetak,
            LocalDate kraj,
            List<Dan> dani,
            int daniSUnosom,
            int daniUCilju,
            NutritivneVrijednostiDTO prosjek,
            NutritivneVrijednostiDTO prosjekCilj,
            boolean imaPrethodni,
            boolean imaSljedeci,
            boolean imaPrimjer
    ) {
        this.imaPrimjer = imaPrimjer;
        this.pocetak = pocetak;
        this.kraj = kraj;
        this.dani = dani;
        this.daniSUnosom = daniSUnosom;
        this.daniUCilju = daniUCilju;
        this.prosjek = prosjek;
        this.prosjekCilj = prosjekCilj;
        this.imaPrethodni = imaPrethodni;
        this.imaSljedeci = imaSljedeci;
    }

    public LocalDate getPocetak() {
        return pocetak;
    }

    public LocalDate getKraj() {
        return kraj;
    }

    public List<Dan> getDani() {
        return dani;
    }

    public int getDaniSUnosom() {
        return daniSUnosom;
    }

    public int getDaniUCilju() {
        return daniUCilju;
    }

    public NutritivneVrijednostiDTO getProsjek() {
        return prosjek;
    }

    public NutritivneVrijednostiDTO getProsjekCilj() {
        return prosjekCilj;
    }

    public boolean getImaPrethodni() {
        return imaPrethodni;
    }

    public boolean getImaSljedeci() {
        return imaSljedeci;
    }

    /** Tjedan sadrži barem jedan obrok s izvorom PRIMJER. */
    public boolean getImaPrimjer() {
        return imaPrimjer;
    }

    /** Dan tjedna; [ciljKalorije] je null kad za taj dan nema preporuke. */
    public static class Dan {

        private LocalDate datum;
        private BigDecimal kalorije;
        private BigDecimal proteini;
        private BigDecimal ugljikohidrati;
        private BigDecimal masti;
        private BigDecimal ciljKalorije;
        private boolean imaUnos;
        private boolean uCilju;

        public Dan() {
        }

        public Dan(
                LocalDate datum,
                BigDecimal kalorije,
                BigDecimal proteini,
                BigDecimal ugljikohidrati,
                BigDecimal masti,
                BigDecimal ciljKalorije,
                boolean imaUnos,
                boolean uCilju
        ) {
            this.datum = datum;
            this.kalorije = kalorije;
            this.proteini = proteini;
            this.ugljikohidrati = ugljikohidrati;
            this.masti = masti;
            this.ciljKalorije = ciljKalorije;
            this.imaUnos = imaUnos;
            this.uCilju = uCilju;
        }

        public LocalDate getDatum() {
            return datum;
        }

        public BigDecimal getKalorije() {
            return kalorije;
        }

        public BigDecimal getProteini() {
            return proteini;
        }

        public BigDecimal getUgljikohidrati() {
            return ugljikohidrati;
        }

        public BigDecimal getMasti() {
            return masti;
        }

        public BigDecimal getCiljKalorije() {
            return ciljKalorije;
        }

        public boolean getImaUnos() {
            return imaUnos;
        }

        /** Bez anotacije Jackson bi ime "UCilju" pretvorio u "ucilju". */
        @JsonProperty("uCilju")
        public boolean getUCilju() {
            return uCilju;
        }
    }
}
