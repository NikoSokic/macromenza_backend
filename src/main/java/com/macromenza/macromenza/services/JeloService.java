package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.CestoJedenoDTO;
import com.macromenza.macromenza.dto.JeloDTO;
import com.macromenza.macromenza.dto.JeloDetaljDTO;
import com.macromenza.macromenza.dto.JeloSastojakDTO;
import com.macromenza.macromenza.dto.KategorijaJelaDTO;
import com.macromenza.macromenza.model.Jelo;
import com.macromenza.macromenza.model.JeloNutritivno;
import com.macromenza.macromenza.repository.JeloNutritivnoRepository;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.JeloSastojakNutritivnoRepository;
import com.macromenza.macromenza.repository.KonzumacijaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JeloService {

    private static final int DANI_CESTO = 60;
    private static final int MAX_CESTO = 50;

    private final JeloRepository jeloRepository;
    private final JeloNutritivnoRepository jeloNutritivnoRepository;
    private final KonzumacijaRepository konzumacijaRepository;
    private final JeloSastojakNutritivnoRepository jeloSastojakNutritivnoRepository;

    public JeloService(
            JeloRepository jeloRepository,
            JeloNutritivnoRepository jeloNutritivnoRepository,
            KonzumacijaRepository konzumacijaRepository,
            JeloSastojakNutritivnoRepository jeloSastojakNutritivnoRepository
    ) {
        this.jeloRepository = jeloRepository;
        this.jeloNutritivnoRepository = jeloNutritivnoRepository;
        this.konzumacijaRepository = konzumacijaRepository;
        this.jeloSastojakNutritivnoRepository = jeloSastojakNutritivnoRepository;
    }

    /** Tekst, kategorija i rasponi makronutrijenata kombiniraju se (I); prazan ili null parametar ne filtrira. */
    public List<JeloDTO> dohvatiJela(
            String search, String kategorija,
            BigDecimal minKalorije, BigDecimal maxKalorije,
            BigDecimal minProteini, BigDecimal maxProteini,
            BigDecimal minUgljikohidrati, BigDecimal maxUgljikohidrati,
            BigDecimal minMasti, BigDecimal maxMasti
    ) {
        List<Jelo> jela;

        if (search != null && !search.isBlank()) {
            jela = jeloRepository.findByAktivnoTrueAndNazivContainingIgnoreCaseOrderByNaziv(search.trim());
        } else {
            jela = jeloRepository.findByAktivnoTrueOrderByNaziv();
        }

        if (kategorija != null && !kategorija.isBlank()) {
            String trazena = kategorija.trim();
            jela = jela.stream()
                    .filter(j -> j.getKategorija() != null && j.getKategorija().equalsIgnoreCase(trazena))
                    .toList();
        }

        return uDtoLista(jela)
                .stream()
                .filter(j -> uOpsegu(j.getKalorije(), minKalorije, maxKalorije))
                .filter(j -> uOpsegu(j.getProteini(), minProteini, maxProteini))
                .filter(j -> uOpsegu(j.getUgljikohidrati(), minUgljikohidrati, maxUgljikohidrati))
                .filter(j -> uOpsegu(j.getMasti(), minMasti, maxMasti))
                .toList();
    }

    public List<KategorijaJelaDTO> dohvatiKategorije() {
        return jeloRepository.findKategorije();
    }

    /** Jela iz obroka korisnika u zadnjih 60 dana (Europe/Zagreb), najčešća prva; jedan upit. */
    public List<CestoJedenoDTO> dohvatiCestoJedeno(Long idKorisnik, int limit) {
        int velicina = Math.min(Math.max(limit, 1), MAX_CESTO);
        LocalDate od = LocalDate.now(ObrokService.ZAGREB).minusDays(DANI_CESTO);

        return konzumacijaRepository.findCestoJedeno(idKorisnik, od, Limit.of(velicina))
                .stream()
                .map(r -> new CestoJedenoDTO(
                        new JeloDTO(
                                r.getIdJelo(), r.getNaziv(), r.getOpis(),
                                r.getKalorije(), r.getProteini(), r.getUgljikohidrati(), r.getMasti(),
                                r.getSifra(), r.getKategorija(), r.getCijenaEur(), r.getNapomena()
                        ),
                        r.getBrojPuta(),
                        r.getZadnjiPut()
                ))
                .toList();
    }

    /** Detalj jela: jedan upit za jelo i vrijednosti, jedan za sastojke (sortirani po gramima silazno). */
    public Optional<JeloDetaljDTO> dohvatiDetalj(Long id) {
        return jeloRepository.findDetalj(id).map(r -> new JeloDetaljDTO(
                r.getIdJelo(), r.getNaziv(), r.getOpis(),
                r.getKalorije(), r.getProteini(), r.getUgljikohidrati(), r.getMasti(),
                r.getSifra(), r.getKategorija(), r.getCijenaEur(), r.getNapomena(),
                r.getMasaG(),
                jeloSastojakNutritivnoRepository.findByIdJeloOrderByGramiDescIdJeloSastojakAsc(id)
                        .stream()
                        .map(s -> new JeloSastojakDTO(
                                s.getNazivURecepturi(), s.getNamirnicaKljuc(), s.getGrami(),
                                s.getKalorije(), s.getProteini(), s.getUgljikohidrati(), s.getMasti()
                        ))
                        .toList()
        ));
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
