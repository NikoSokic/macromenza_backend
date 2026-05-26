package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.services.IzracunPreporukeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preporuke")
public class IzracunPreporukeController {

    private final IzracunPreporukeService izracunPreporukeService;

    public IzracunPreporukeController(IzracunPreporukeService izracunPreporukeService) {
        this.izracunPreporukeService = izracunPreporukeService;
    }

    @PostMapping("/izracunaj/{korisnikId}")
    public Preporuka izracunajPreporuku(@PathVariable Long korisnikId) {
        return izracunPreporukeService.izracunajPreporuku(korisnikId);
    }
}

