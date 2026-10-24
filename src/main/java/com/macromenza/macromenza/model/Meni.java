package com.macromenza.macromenza.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meni")
public class Meni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sifra;

    @Column(name = "tip_obroka")
    private String tipObroka;

    private Boolean vegetarijanski;

    @Column(name = "redni_broj")
    private Integer redniBroj;

    @OneToMany(mappedBy = "meni")
    @OrderBy("redoslijed")
    private List<MeniStavka> stavke = new ArrayList<>();

    public Meni() {
    }

    public Long getId() {
        return id;
    }

    public String getSifra() {
        return sifra;
    }

    public String getTipObroka() {
        return tipObroka;
    }

    public Boolean getVegetarijanski() {
        return vegetarijanski;
    }

    public Integer getRedniBroj() {
        return redniBroj;
    }

    public List<MeniStavka> getStavke() {
        return stavke;
    }
}
