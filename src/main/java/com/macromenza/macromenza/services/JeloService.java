package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.JeloDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JeloService {

    private final JeloRepository jeloRepository;
    private final JeloNutritivnoRepository jeloNutritivnoRepository;

    public JeloService(
            JeloRepository jeloRepository,
            JeloNutritivnoRepository jeloNutritivnoRepository
    ) {
        this.jeloRepository = jeloRepository;
        this.jeloNutritivnoRepository = jeloNutritivnoRepository;
    }

    public List<JeloDTO> dohvatiJela(String search) {
        List<Jelo> jela;

        if (search != null && !search.isBlank()) {
            jela = jeloRepository.findByAktivnoTrueAndNazivContainingIgnoreCaseOrderByNaziv(search.trim());
        } else {
            jela = jeloRepository.findByAktivnoTrueOrderByNaziv();
        }

        return uDtoLista(jela);
    }

    public Optional<JeloDTO> dohvatiJelo(Long id) {
        return jeloRepository.findById(id).map(jelo ->
                uDto(jelo, jeloNutritivnoRepository.findById(id).orElse(null))
        );
    }

    public List<JeloDTO> filtrirajJela(
            BigDecimal minKalorije, BigDecimal maxKalorije,
            BigDecimal minProteini, BigDecimal maxProteini,
            BigDecimal minUgljikohidrati, BigDecimal maxUgljikohidrati,
            BigDecimal minMasti, BigDecimal maxMasti
    ) {
        return uDtoLista(jeloRepository.findByAktivnoTrueOrderByNaziv())
                .stream()
                .filter(j -> uOpsegu(j.getKalorije(), minKalorije, maxKalorije))
                .filter(j -> uOpsegu(j.getProteini(), minProteini, maxProteini))
                .filter(j -> uOpsegu(j.getUgljikohidrati(), minUgljikohidrati, maxUgljikohidrati))
                .filter(j -> uOpsegu(j.getMasti(), minMasti, maxMasti))
                .toList();
    }

    private boolean uOpsegu(BigDecimal vrijednost, BigDecimal min, BigDecimal max) {
        if (vrijednost == null) {
            return min == null && max == null;
        }

        return (min == null || vrijednost.compareTo(min) >= 0)
                && (max == null || vrijednost.compareTo(max) <= 0);
    }

    private List<JeloDTO> uDtoLista(List<Jelo> jela) {
        Map<Long, JeloNutritivno> nutritivno = jeloNutritivnoRepository
                .findAllById(jela.stream().map(Jelo::getId).toList())
                .stream()
                .collect(Collectors.toMap(JeloNutritivno::getIdJelo, Function.identity()));

        return jela.stream().map(j -> uDto(j, nutritivno.get(j.getId()))).toList();
    }

    private JeloDTO uDto(Jelo j, JeloNutritivno n) {
        return new JeloDTO(
                j.getId(),
                j.getNaziv(),
                j.getOpis(),
                n != null ? n.getKalorije() : null,
                n != null ? n.getProteini() : null,
                n != null ? n.getUgljikohidrati() : null,
                n != null ? n.getMasti() : null,
                j.getSifra(),
                j.getKategorija(),
                j.getCijenaEur(),
                j.getNapomena()
        );
    }
}
