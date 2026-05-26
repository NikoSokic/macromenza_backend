package com.macromenza.macromenza.model;

import jakarta.persistence.*;

@Entity
@Table(name = "profil_podaci")
public class ProfilPodaci {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_korisnik")
    private Long idKorisnik;

    private Integer visina;
    private String spol;
    private Integer dob;

    public ProfilPodaci() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdKorisnik() {
        return idKorisnik;
    }

    public Integer getVisina() {
        return visina;
    }

    public String getSpol() {
        return spol;
    }

    public Integer getDob() {
        return dob;
    }

    public void setIdKorisnik(Long idKorisnik) {
        this.idKorisnik = idKorisnik;
    }

    public void setVisina(Integer visina) {
        this.visina = visina;
    }

    public void setSpol(String spol) {
        this.spol = spol;
    }

    public void setDob(Integer dob) {
        this.dob = dob;
    }
}