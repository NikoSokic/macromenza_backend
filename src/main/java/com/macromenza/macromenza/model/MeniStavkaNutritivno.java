package com.macromenza.macromenza.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "v_meni_stavka_nutritivno")
public class MeniStavkaNutritivno {

    @Id
    @Column(name = "id_meni_stavka")
    private Long idMeniStavka;

    @Column(name = "id_meni")
    private Long idMeni;

    private BigDecimal kalorije;
    private BigDecimal proteini;
    private BigDecimal ugljikohidrati;
    private BigDecimal masti;

    public MeniStavkaNutritivno() {
    }

    public Long getIdMeniStavka() {
        return idMeniStavka;
    }

    public Long getIdMeni() {
        return idMeni;
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
