package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.IzracunDTO;
import com.macromenza.macromenza.dto.IzracunPreporukeRequest;
import com.macromenza.macromenza.services.IzracunPreporukeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/preporuke")
public class IzracunPreporukeController {

    private final IzracunPreporukeService izracunPreporukeService;

    public IzracunPreporukeController(IzracunPreporukeService izracunPreporukeService) {
        this.izracunPreporukeService = izracunPreporukeService;
    }

    /** Koraci izračuna za zadane ulaze; ništa se ne sprema. */
    @PostMapping("/pregled")
    public ResponseEntity<?> pregled(@RequestBody IzracunPreporukeRequest r) {
        String greska = provjeri(r);

        if (greska != null) {
            return ResponseEntity.badRequest().body(Map.of("poruka", greska));
        }

        IzracunDTO izracun = izracunPreporukeService.izracunaj(
                r.getSpol(), r.getDob(), r.getVisina(), r.getMasa(), r.getRazinaAktivnosti(), r.getTipCilja()
        );
        return ResponseEntity.ok(izracun);
    }

    private String provjeri(IzracunPreporukeRequest r) {
        if (r.getSpol() == null || !(r.getSpol().equalsIgnoreCase("M") || r.getSpol().equalsIgnoreCase("Z"))) {
            return "Spol mora biti M ili Z.";
        }
        if (r.getDob() == null || r.getDob() < 15 || r.getDob() > 100) {
            return "Dob mora biti između 15 i 100 godina.";
        }
        if (r.getVisina() == null || r.getVisina() < 100 || r.getVisina() > 250) {
            return "Visina mora biti između 100 i 250 cm.";
        }
        if (r.getMasa() == null || r.getMasa() < 20 || r.getMasa() > 400) {
            return "Masa mora biti između 20 i 400 kg.";
        }
        if (r.getRazinaAktivnosti() == null
                || !Set.of("sjedilacka", "lagana", "umjerena", "visoka").contains(r.getRazinaAktivnosti().toLowerCase())) {
            return "Razina aktivnosti mora biti sjedilacka, lagana, umjerena ili visoka.";
        }
        if (r.getTipCilja() == null
                || !Set.of("mrsavljenje", "odrzavanje", "dobivanje_mase").contains(r.getTipCilja().toLowerCase())) {
            return "Tip cilja mora biti mrsavljenje, odrzavanje ili dobivanje_mase.";
        }
        return null;
    }
}

