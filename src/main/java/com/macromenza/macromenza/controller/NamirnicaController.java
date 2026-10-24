package com.macromenza.macromenza.controller;

import com.macromenza.macromenza.dto.NamirnicaDTO;
import com.macromenza.macromenza.services.NamirnicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/namirnice")
public class NamirnicaController {

    private final NamirnicaService namirnicaService;

    public NamirnicaController(NamirnicaService namirnicaService) {
        this.namirnicaService = namirnicaService;
    }

    @GetMapping
    public List<NamirnicaDTO> getNamirnice(
            @RequestParam(required = false) String q
    ) {
        return namirnicaService.dohvatiNamirnice(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NamirnicaDTO> getNamirnicaPoId(@PathVariable Long id) {
        return ResponseEntity.of(namirnicaService.dohvatiNamirnicu(id));
    }
}
