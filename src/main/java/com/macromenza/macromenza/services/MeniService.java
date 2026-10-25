package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.MeniDTO;
import com.macromenza.macromenza.dto.MeniStavkaDTO;
import com.macromenza.macromenza.model.Meni;
import com.macromenza.macromenza.model.MeniNutritivno;
import com.macromenza.macromenza.model.MeniStavka;
import com.macromenza.macromenza.model.MeniStavkaNutritivno;
import com.macromenza.macromenza.repository.MeniNutritivnoRepository;
import com.macromenza.macromenza.repository.MeniRepository;
import com.macromenza.macromenza.repository.MeniStavkaNutritivnoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MeniService {

    private final MeniRepository meniRepository;
    private final MeniNutritivnoRepository meniNutritivnoRepository;
    private final MeniStavkaNutritivnoRepository meniStavkaNutritivnoRepository;

    public MeniService(
            MeniRepository meniRepository,
            MeniNutritivnoRepository meniNutritivnoRepository,
            MeniStavkaNutritivnoRepository meniStavkaNutritivnoRepository
    ) {
        this.meniRepository = meniRepository;
        this.meniNutritivnoRepository = meniNutritivnoRepository;
        this.meniStavkaNutritivnoRepository = meniStavkaNutritivnoRepository;
    }

    public List<MeniDTO> dohvatiMenije(String tip, Boolean vegetarijanski) {
        String tipObroka = (tip != null && !tip.isBlank()) ? tip.trim().toUpperCase() : null;
        return uDtoLista(meniRepository.pronadiMenije(tipObroka, vegetarijanski));
    }

    public Optional<MeniDTO> dohvatiMeni(Long id) {
        return meniRepository.pronadiPoId(id).map(m -> uDtoLista(List.of(m)).get(0));
    }

    private List<MeniDTO> uDtoLista(List<Meni> meniji) {
        if (meniji.isEmpty()) {
            return List.of();
        }

        Map<Long, MeniNutritivno> totali = meniNutritivnoRepository
                .findAllById(meniji.stream().map(Meni::getId).toList())
                .stream()
                .collect(Collectors.toMap(MeniNutritivno::getIdMeni, Function.identity()));

        Map<Long, MeniStavkaNutritivno> vrijednostiStavki = meniStavkaNutritivnoRepository
                .findAllById(meniji.stream().flatMap(m -> m.getStavke().stream()).map(MeniStavka::getId).toList())
                .stream()
                .collect(Collectors.toMap(MeniStavkaNutritivno::getIdMeniStavka, Function.identity()));

        return meniji.stream()
                .map(m -> uDto(m, totali.get(m.getId()), vrijednostiStavki))
                .toList();
    }

    private MeniDTO uDto(Meni m, MeniNutritivno n, Map<Long, MeniStavkaNutritivno> vrijednostiStavki) {
        List<MeniStavkaDTO> stavke = m.getStavke().stream()
                .map(s -> uDto(s, vrijednostiStavki.get(s.getId())))
                .toList();

        return new MeniDTO(
                m.getId(),
                m.getSifra(),
                m.getTipObroka(),
                m.getVegetarijanski(),
                m.getRedniBroj(),
                n != null ? n.getKalorije() : null,
                n != null ? n.getProteini() : null,
                n != null ? n.getUgljikohidrati() : null,
                n != null ? n.getMasti() : null,
                stavke
        );
    }

    private MeniStavkaDTO uDto(MeniStavka s, MeniStavkaNutritivno n) {
        return new MeniStavkaDTO(
                s.getJelo().getId(),
                s.getOznaka(),
                s.getJelo().getNaziv(),
                s.getKolicina(),
                s.getPriblizno(),
                n != null ? n.getKalorije() : null,
                n != null ? n.getProteini() : null,
                n != null ? n.getUgljikohidrati() : null,
                n != null ? n.getMasti() : null
        );
    }
}
