package com.macromenza.macromenza.dto;

/** Ulaz za POST /api/preporuke/pregled; ništa se ne sprema. */
public class IzracunPreporukeRequest {

    private String spol;
    private Integer dob;
    private Integer visina;
    private Double masa;
    private String razinaAktivnosti;
    private String tipCilja;

    public String getSpol() {
        return spol;
    }

    public void setSpol(String spol) {
        this.spol = spol;
    }

    public Integer getDob() {
        return dob;
    }

    public void setDob(Integer dob) {
        this.dob = dob;
    }

    public Integer getVisina() {
        return visina;
    }

    public void setVisina(Integer visina) {
        this.visina = visina;
    }

    public Double getMasa() {
        return masa;
    }

    public void setMasa(Double masa) {
        this.masa = masa;
    }

    public String getRazinaAktivnosti() {
        return razinaAktivnosti;
    }

    public void setRazinaAktivnosti(String razinaAktivnosti) {
        this.razinaAktivnosti = razinaAktivnosti;
    }

    public String getTipCilja() {
        return tipCilja;
    }

    public void setTipCilja(String tipCilja) {
        this.tipCilja = tipCilja;
    }
}
