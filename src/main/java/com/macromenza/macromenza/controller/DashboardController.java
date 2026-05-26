package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.DashboardDTO;
import com.macromenza.macromenza.services.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{idKorisnik}")
    public DashboardDTO dohvatiDashboard(@PathVariable Long idKorisnik) {
        return dashboardService.dohvatiDashboard(idKorisnik);
    }
}