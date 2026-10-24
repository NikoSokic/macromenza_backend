package com.macromenza.macromenza.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "meni_stavka")
public class MeniStavka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_meni")
    private Meni meni;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_jelo")
    private Jelo jelo;

    private Integer redoslijed;
    private String oznaka;
    private BigDecimal kolicina;
    private Boolean priblizno;

    public MeniStavka() {
    }

    public Long getId() {
        return id;
    }

    public Meni getMeni() {
        return meni;
    }

    public Jelo getJelo() {
        return jelo;
    }

    public Integer getRedoslijed() {
        return redoslijed;
    }

    public String getOznaka() {
        return oznaka;
    }

    public BigDecimal getKolicina() {
        return kolicina;
    }

    public Boolean getPriblizno() {
        return priblizno;
    }
}
