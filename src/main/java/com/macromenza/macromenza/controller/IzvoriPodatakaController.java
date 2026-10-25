package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.MeniRepository;
import com.macromenza.macromenza.repository.NamirnicaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Brojevi iza ekrana "Odakle su kalorije". */
@RestController
@RequestMapping("/api/izvori-podataka")
public class IzvoriPodatakaController {

    private final JeloRepository jeloRepository;
    private final NamirnicaRepository namirnicaRepository;
    private final MeniRepository meniRepository;

    public IzvoriPodatakaController(
            JeloRepository jeloRepository,
            NamirnicaRepository namirnicaRepository,
            MeniRepository meniRepository
    ) {
        this.jeloRepository = jeloRepository;
        this.namirnicaRepository = namirnicaRepository;
        this.meniRepository = meniRepository;
    }

    @GetMapping
    public Map<String, Object> getIzvori() {
        Map<String, Object> odgovor = new LinkedHashMap<>();
        odgovor.put("brojJela", jeloRepository.countByAktivnoTrue());
        odgovor.put("brojNamirnica", namirnicaRepository.count());
        odgovor.put("brojMenija", meniRepository.count());
        odgovor.put("izvorNormativa", "NN 120/2013");
        return odgovor;
    }
}
