package com.macromenza.macromenza.dto;

import java.time.LocalDate;

public class CestoJedenoDTO {

    private JeloDTO jelo;
    private Long brojPuta;
    private LocalDate zadnjiPut;

    public CestoJedenoDTO() {
    }

    public CestoJedenoDTO(JeloDTO jelo, Long brojPuta, LocalDate zadnjiPut) {
        this.jelo = jelo;
        this.brojPuta = brojPuta;
        this.zadnjiPut = zadnjiPut;
    }

    public JeloDTO getJelo() {
        return jelo;
    }

    public void setJelo(JeloDTO jelo) {
        this.jelo = jelo;
    }

    public Long getBrojPuta() {
        return brojPuta;
    }

    public void setBrojPuta(Long brojPuta) {
        this.brojPuta = brojPuta;
    }

    public LocalDate getZadnjiPut() {
        return zadnjiPut;
    }

    public void setZadnjiPut(LocalDate zadnjiPut) {
        this.zadnjiPut = zadnjiPut;
    }
}
