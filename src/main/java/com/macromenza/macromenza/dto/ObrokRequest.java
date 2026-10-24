package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ObrokRequest {

    private Long idKorisnik;
    private LocalDate datum;
    private LocalTime vrijeme;
    private String tipObroka;
    private String izvor;
    private List<Stavka> stavke;

    public ObrokRequest() {
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(LocalTime vrijeme) {
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

    public static class Stavka {

        private Long idJelo;
        private BigDecimal kolicina;

        public Stavka() {
        }

        public Stavka(Long idJelo, BigDecimal kolicina) {
            this.idJelo = idJelo;
            this.kolicina = kolicina;
        }

        public Long getIdJelo() {
            return idJelo;
        }

        public void setIdJelo(Long idJelo) {
            this.idJelo = idJelo;
        }

        public BigDecimal getKolicina() {
            return kolicina;
        }

        public void setKolicina(BigDecimal kolicina) {
            this.kolicina = kolicina;
        }
    }
}
