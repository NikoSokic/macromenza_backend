package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.security.TrenutniKorisnik;
import com.macromenza.macromenza.dto.DashboardDTO;
import com.macromenza.macromenza.services.DashboardService;
import org.springframework.web.bind.annotation.*;

/** Zamijenjen s GET /api/obroci/dan-pregled; Home ga više ne koristi. */
@Deprecated
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final TrenutniKorisnik trenutni;

    public DashboardController(DashboardService dashboardService, TrenutniKorisnik trenutni) {
        this.trenutni = trenutni;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{idKorisnik}")
    public DashboardDTO dohvatiDashboard(@PathVariable Long idKorisnik) {
        trenutni.provjeri(idKorisnik);
        return dashboardService.dohvatiDashboard(idKorisnik);
    }
}