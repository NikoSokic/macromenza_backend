package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.NamirnicaDTO;
import com.macromenza.macromenza.model.Namirnica;
import com.macromenza.macromenza.repository.NamirnicaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NamirnicaService {

    private final NamirnicaRepository namirnicaRepository;

    public NamirnicaService(NamirnicaRepository namirnicaRepository) {
        this.namirnicaRepository = namirnicaRepository;
    }

    public List<NamirnicaDTO> dohvatiNamirnice(String q) {
        List<Namirnica> namirnice;

        if (q != null && !q.isBlank()) {
            namirnice = namirnicaRepository.findByKljucContainingIgnoreCaseOrderByKljuc(q.trim());
        } else {
            namirnice = namirnicaRepository.findAllByOrderByKljuc();
        }

        return namirnice.stream().map(this::uDto).toList();
    }

    public Optional<NamirnicaDTO> dohvatiNamirnicu(Long id) {
        return namirnicaRepository.findById(id).map(this::uDto);
    }

    private NamirnicaDTO uDto(Namirnica n) {
        return new NamirnicaDTO(
                n.getId(),
                n.getKljuc(),
                n.getKalorije(),
                n.getProteini(),
                n.getUgljikohidrati(),
                n.getMasti(),
                n.getGramaPoKomadu(),
                n.getNapomena()
        );
    }
}
