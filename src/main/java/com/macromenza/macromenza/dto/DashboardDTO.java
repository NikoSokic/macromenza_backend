package com.macromenza.macromenza.dto;

public class DashboardDTO {

    private NutritivneVrijednostiDTO preporuka;
    private NutritivneVrijednostiDTO pojedeno;
    private NutritivneVrijednostiDTO preostalo;

    public DashboardDTO() {
    }

    public DashboardDTO(
            NutritivneVrijednostiDTO preporuka,
            NutritivneVrijednostiDTO pojedeno,
            NutritivneVrijednostiDTO preostalo
    ) {
        this.preporuka = preporuka;
        this.pojedeno = pojedeno;
        this.preostalo = preostalo;
    }

    public NutritivneVrijednostiDTO getPreporuka() {
        return preporuka;
    }

    public void setPreporuka(NutritivneVrijednostiDTO preporuka) {
        this.preporuka = preporuka;
    }

    public NutritivneVrijednostiDTO getPojedeno() {
        return pojedeno;
    }

    public void setPojedeno(NutritivneVrijednostiDTO pojedeno) {
        this.pojedeno = pojedeno;
    }

    public NutritivneVrijednostiDTO getPreostalo() {
        return preostalo;
    }

    public void setPreostalo(NutritivneVrijednostiDTO preostalo) {
        this.preostalo = preostalo;
    }
}