package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.MeniDTO;
import com.macromenza.macromenza.services.MeniService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meniji")
public class MeniController {

    private final MeniService meniService;

    public MeniController(MeniService meniService) {
        this.meniService = meniService;
    }

    @GetMapping
    public List<MeniDTO> getMeniji(
            @RequestParam(required = false) String tip,
            @RequestParam(required = false) Boolean vegetarijanski
    ) {
        return meniService.dohvatiMenije(tip, vegetarijanski);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeniDTO> getMeniPoId(@PathVariable Long id) {
        return ResponseEntity.of(meniService.dohvatiMeni(id));
    }
}
