package com.macromenza.macromenza.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MjerenjeMaseDTO {

    private Long id;
    private LocalDate datum;
    private BigDecimal masa;

    public MjerenjeMaseDTO() {
    }

    public MjerenjeMaseDTO(Long id, LocalDate datum, BigDecimal masa) {
        this.id = id;
        this.datum = datum;
        this.masa = masa;
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

    public BigDecimal getMasa() {
        return masa;
    }

    public void setMasa(BigDecimal masa) {
        this.masa = masa;
    }
}
