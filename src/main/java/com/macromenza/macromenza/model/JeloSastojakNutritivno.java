package com.macromenza.macromenza.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "v_jelo_sastojak_nutritivno")
public class JeloSastojakNutritivno {

    @Id
    @Column(name = "id_jelo_sastojak")
    private Long idJeloSastojak;

    @Column(name = "id_jelo")
    private Long idJelo;

    @Column(name = "naziv_u_recepturi")
    private String nazivURecepturi;

    @Column(name = "namirnica_kljuc")
    private String namirnicaKljuc;

    private BigDecimal grami;
    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public JeloSastojakNutritivno() {
    }

    public Long getIdJeloSastojak() {
        return idJeloSastojak;
    }

    public Long getIdJelo() {
        return idJelo;
    }

    public String getNazivURecepturi() {
        return nazivURecepturi;
    }

    public String getNamirnicaKljuc() {
        return namirnicaKljuc;
    }

    public BigDecimal getGrami() {
        return grami;
    }

    public BigDecimal getKalorije() {
        return kalorije;
    }

    public BigDecimal getProteini() {
        return proteini;
    }

    public BigDecimal getUgljikohidrati() {
        return ugljikohidrati;
    }

    public BigDecimal getMasti() {
        return masti;
    }
}
