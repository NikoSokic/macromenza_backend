package com.macromenza.macromenza.dto;

/**
 * Koraci izračuna dnevne preporuke. [bmr], [tdee] i [korekcijaCilja] su na 1 decimalu; [ciljKalorije], [proteini],
 * [masti] i [ugljikohidrati] su zaokruženi na cijele brojeve, točno kao spremljena preporuka.
 */
public class IzracunDTO {

    private double bmr;
    private String bmrFormula;
    private double faktorAktivnosti;
    private double tdee;
    private double korekcijaCilja;
    private int ciljKalorije;
    private int proteini;
    private int masti;
    private int ugljikohidrati;

    public IzracunDTO(
            double bmr,
            String bmrFormula,
            double faktorAktivnosti,
            double tdee,
            double korekcijaCilja,
            int ciljKalorije,
            int proteini,
            int masti,
            int ugljikohidrati
    ) {
        this.bmr = bmr;
        this.bmrFormula = bmrFormula;
        this.faktorAktivnosti = faktorAktivnosti;
        this.tdee = tdee;
        this.korekcijaCilja = korekcijaCilja;
        this.ciljKalorije = ciljKalorije;
        this.proteini = proteini;
        this.masti = masti;
        this.ugljikohidrati = ugljikohidrati;
    }

    public double getBmr() {
        return bmr;
    }

    public String getBmrFormula() {
        return bmrFormula;
    }

    public double getFaktorAktivnosti() {
        return faktorAktivnosti;
    }

    public double getTdee() {
        return tdee;
    }

    public double getKorekcijaCilja() {
        return korekcijaCilja;
    }

    public int getCiljKalorije() {
        return ciljKalorije;
    }

    public int getProteini() {
        return proteini;
    }

    public int getMasti() {
        return masti;
    }

    public int getUgljikohidrati() {
        return ugljikohidrati;
    }
}
