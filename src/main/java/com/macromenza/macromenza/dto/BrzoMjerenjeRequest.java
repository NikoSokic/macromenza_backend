package com.macromenza.macromenza.dto;

import java.math.BigDecimal;

public class BrzoMjerenjeRequest {

    private Long idKorisnik;
    private BigDecimal masa;

    public BrzoMjerenjeRequest() {
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public BigDecimal getMasa() {
        return masa;
    }

    public void setMasa(BigDecimal masa) {
        this.masa = masa;
    }
}
