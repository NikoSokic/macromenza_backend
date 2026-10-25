package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.DashboardDTO;
import com.macromenza.macromenza.dto.NutritivneVrijednostiDTO;
import com.macromenza.macromenza.dto.UkupniUnosDTO;
import com.macromenza.macromenza.model.Preporuka;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;



/** Zamijenjen s ObrokService.dohvatiDanPregled; Home ga više ne koristi. */
@Deprecated
@Service
public class DashboardService {

    private final IzracunPreporukeService izracunPreporukeService;
    private final KonzumacijaService konzumacijaService;

    public DashboardService(
            IzracunPreporukeService izracunPreporukeService,
            KonzumacijaService konzumacijaService
    ) {
        this.izracunPreporukeService = izracunPreporukeService;
        this.konzumacijaService = konzumacijaService;
    }

    public DashboardDTO dohvatiDashboard(Long idKorisnik) {

        Preporuka preporukaBaza = izracunPreporukeService.dohvatiZadnjuPreporuku(idKorisnik);

        UkupniUnosDTO pojedenoBaza = konzumacijaService.izracunajDnevniUnos(idKorisnik);

        NutritivneVrijednostiDTO preporuka =
                new NutritivneVrijednostiDTO(
                        BigDecimal.valueOf(preporukaBaza.getKalorije()),
                        BigDecimal.valueOf(preporukaBaza.getProteini()),
                        BigDecimal.valueOf(preporukaBaza.getUgljikohidrati()),
                        BigDecimal.valueOf(preporukaBaza.getMasti())
                );

        NutritivneVrijednostiDTO pojedeno =
                new NutritivneVrijednostiDTO(
                        pojedenoBaza.getKalorije(),
                        pojedenoBaza.getProteini(),
                        pojedenoBaza.getUgljikohidrati(),
                        pojedenoBaza.getMasti()
                );

        NutritivneVrijednostiDTO preostalo =
                new NutritivneVrijednostiDTO(
                        preporuka.getKalorije().subtract(pojedeno.getKalorije()),
                        preporuka.getProteini().subtract(pojedeno.getProteini()),
                        preporuka.getUgljikohidrati().subtract(pojedeno.getUgljikohidrati()),
                        preporuka.getMasti().subtract(pojedeno.getMasti())
                );

        return new DashboardDTO(preporuka, pojedeno, preostalo);
    }

}