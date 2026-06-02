package com.macromenza.macromenza.dto;

import java.util.UUID;

public class RegistracijaProfilRequest {

    private UUID supabaseUid;
    private String email;
    private String ime;
    private String prezime;

    private String spol;
    private Integer visina;
    private Integer dob;

    private Double masa;
    private String razinaAktivnosti;
    private String tipCilja;

    public UUID getSupabaseUid() {
        return supabaseUid;
    }

    public void setSupabaseUid(UUID supabaseUid) {
        this.supabaseUid = supabaseUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getSpol() {
        return spol;
    }

    public void setSpol(String spol) {
        this.spol = spol;
    }

    public Integer getVisina() {
        return visina;
    }

    public void setVisina(Integer visina) {
        this.visina = visina;
    }

    public Integer getDob() {
        return dob;
    }

    public void setDob(Integer dob) {
        this.dob = dob;
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