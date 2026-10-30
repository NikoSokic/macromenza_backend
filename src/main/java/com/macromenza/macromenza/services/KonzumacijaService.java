package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.PovijestDanaDTO;
import com.macromenza.macromenza.dto.StavkaObrokaDTO;
import com.macromenza.macromenza.model.Konzumacija;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KonzumacijaService {

    private final KonzumacijaRepository konzumacijaRepository;
    private final JeloRepository jeloRepository;
    private final JeloNutritivnoRepository jeloNutritivnoRepository;

    public KonzumacijaService(
            KonzumacijaRepository konzumacijaRepository,
            JeloRepository jeloRepository,
            JeloNutritivnoRepository jeloNutritivnoRepository
    ) {
        this.konzumacijaRepository = konzumacijaRepository;
        this.jeloRepository = jeloRepository;
        this.jeloNutritivnoRepository = jeloNutritivnoRepository;
    }

    public PovijestDanaDTO dohvatiPovijestZaDan(
            Long idKorisnik,
            LocalDate datum
    ) {

        List<Konzumacija> lista = konzumacijaRepository.findZaDan(idKorisnik, datum);

        List<StavkaObrokaDTO> stavke =
                lista.stream()
                        .map(k -> {

                            Jelo jelo = jeloRepository
                                    .findById(k.getIdJelo())
                                    .orElseThrow();

                            JeloNutritivno nutritivno = jeloNutritivnoRepository
                                    .findById(k.getIdJelo())
                                    .orElseThrow();
                            BigDecimal kolicina = k.getKolicina();

                            return new StavkaObrokaDTO(
                                    jelo.getNaziv(),
                                    k.getTip_obroka(),
                                    k.getKolicina(),
                                    nutritivno.getKalorije().multiply(kolicina),
                                    nutritivno.getProteini().multiply(kolicina),
                                    nutritivno.getUgljikohidrati().multiply(kolicina),
                                    nutritivno.getMasti().multiply(kolicina)
                            );
                        })
                        .toList();

        BigDecimal kalorije = stavke.stream()
                        .map(StavkaObrokaDTO::getKalorije)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal proteini = stavke.stream()
                        .map(StavkaObrokaDTO::getProteini)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ugljikohidrati = stavke.stream()
                        .map(StavkaObrokaDTO::getUgljikohidrati)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal masti = stavke.stream()
                        .map(StavkaObrokaDTO::getMasti)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<StavkaObrokaDTO>> obroci = stavke.stream()
                        .collect(Collectors.groupingBy(StavkaObrokaDTO::getTipObroka)
                        );

        return new PovijestDanaDTO(
                datum,
                kalorije,
                proteini,
                ugljikohidrati,
                masti,
                obroci
        );
    }
}