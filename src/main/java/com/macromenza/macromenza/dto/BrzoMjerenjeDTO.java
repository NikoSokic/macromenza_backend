package com.macromenza.macromenza.dto;

import com.macromenza.macromenza.model.Preporuka;

public class BrzoMjerenjeDTO {

    private MjerenjeMaseDTO mjerenje;
    private Preporuka preporuka;

    public BrzoMjerenjeDTO() {
    }

    public BrzoMjerenjeDTO(MjerenjeMaseDTO mjerenje, Preporuka preporuka) {
        this.mjerenje = mjerenje;
        this.preporuka = preporuka;
    }

    public MjerenjeMaseDTO getMjerenje() {
        return mjerenje;
    }

    public void setMjerenje(MjerenjeMaseDTO mjerenje) {
        this.mjerenje = mjerenje;
    }

    public Preporuka getPreporuka() {
        return preporuka;
    }

    public void setPreporuka(Preporuka preporuka) {
        this.preporuka = preporuka;
    }
}
