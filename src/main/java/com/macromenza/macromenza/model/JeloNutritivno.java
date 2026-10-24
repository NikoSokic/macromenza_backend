package com.macromenza.macromenza.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "v_jelo_nutritivno")
public class JeloNutritivno {

    @Id
    @Column(name = "id_jelo")
    private Long idJelo;

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    @Column(name = "masa_g")
    private BigDecimal masaG;

    public JeloNutritivno() {
    }

    public Long getIdJelo() {
        return idJelo;
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

    public BigDecimal getMasaG() {
        return masaG;
    }
}
