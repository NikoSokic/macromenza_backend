package com.macromenza.macromenza.dto;

public class KategorijaJelaDTO {

    private String naziv;
    private Long brojJela;

    public KategorijaJelaDTO() {
    }

    public KategorijaJelaDTO(String naziv, Long brojJela) {
        this.naziv = naziv;
        this.brojJela = brojJela;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Long getBrojJela() {
        return brojJela;
    }

    public void setBrojJela(Long brojJela) {
        this.brojJela = brojJela;
    }
}
