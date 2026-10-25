package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prijedlog obroka iz slike ili opisa; ništa se ne sprema. Korisnik ga potvrđuje s POST /api/obroci
 * (izvor SLIKA ili OPIS). Vrijednosti stavke su iz v_jelo_nutritivno × količina.
 */
public class PrijedlogObrokaDTO {

    private List<Stavka> stavke;
    private List<String> neprepoznato;
    private String napomena;
    private String model;

    public PrijedlogObrokaDTO(List<Stavka> stavke, List<String> neprepoznato, String napomena, String model) {
        this.stavke = stavke;
        this.neprepoznato = neprepoznato;
        this.napomena = napomena;
        this.model = model;
    }

    public List<Stavka> getStavke() {
        return stavke;
    }

    public List<String> getNeprepoznato() {
        return neprepoznato;
    }

    public String getNapomena() {
        return napomena;
    }

    public String getModel() {
        return model;
    }

    public static class Stavka {

        private Long idJelo;
        private String naziv;
        private BigDecimal kolicina;
        private String sigurnost;
        private BigDecimal kalorije;
        private BigDecimal proteini;
        private BigDecimal ugljikohidrati;
        private BigDecimal masti;

        public Stavka(
                Long idJelo,
                String naziv,
                BigDecimal kolicina,
                String sigurnost,
                BigDecimal kalorije,
                BigDecimal proteini,
                BigDecimal ugljikohidrati,
                BigDecimal masti
        ) {
            this.idJelo = idJelo;
            this.naziv = naziv;
            this.kolicina = kolicina;
            this.sigurnost = sigurnost;
            this.kalorije = kalorije;
            this.proteini = proteini;
            this.ugljikohidrati = ugljikohidrati;
            this.masti = masti;
        }

        public Long getIdJelo() {
            return idJelo;
        }

        public String getNaziv() {
            return naziv;
        }

        public BigDecimal getKolicina() {
            return kolicina;
        }

        /** VISOKA, SREDNJA ili NISKA. */
        public String getSigurnost() {
            return sigurnost;
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
    }
}
