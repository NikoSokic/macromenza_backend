package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.BrzoMjerenjeDTO;
import com.macromenza.macromenza.dto.BrzoMjerenjeRequest;
import com.macromenza.macromenza.dto.MjerenjeMaseDTO;
import com.macromenza.macromenza.model.Mjerenje;
import com.macromenza.macromenza.model.Preporuka;
import com.macromenza.macromenza.repository.MjerenjeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MjerenjeService {

    private static final BigDecimal NAJMANJA_MASA = new BigDecimal("20");
    private static final BigDecimal NAJVECA_MASA = new BigDecimal("400");
    private static final LocalDate NAJRANIJI_DATUM = LocalDate.of(1900, 1, 1);
    private static final LocalDate NAJKASNIJI_DATUM = LocalDate.of(9999, 12, 31);

    private final MjerenjeRepository mjerenjeRepository;
    private final IzracunPreporukeService izracunPreporukeService;

    public MjerenjeService(
            MjerenjeRepository mjerenjeRepository,
            IzracunPreporukeService izracunPreporukeService
    ) {
        this.mjerenjeRepository = mjerenjeRepository;
        this.izracunPreporukeService = izracunPreporukeService;
    }

    /** Mase po datumu uzlazno, jedna po danu (zadnje mjerenje dana); [od] i [doDatuma] su opcionalni. */
    public List<MjerenjeMaseDTO> dohvatiMase(Long idKorisnik, LocalDate od, LocalDate doDatuma) {

        List<Mjerenje> mjerenja = mjerenjeRepository.findByIdKorisnikAndDatumBetweenOrderByDatumAscIdAsc(
                idKorisnik,
                od != null ? od : NAJRANIJI_DATUM,
                doDatuma != null ? doDatuma : NAJKASNIJI_DATUM
        );

        // Redovi su poredani po (datum, id), pa zadnji upis za datum pregazi ranije.
        Map<LocalDate, Mjerenje> poDanu = new LinkedHashMap<>();
        for (Mjerenje m : mjerenja) {
            poDanu.put(m.getDatum(), m);
        }

        List<MjerenjeMaseDTO> rezultat = new ArrayList<>();
        for (Mjerenje m : poDanu.values()) {
            rezultat.add(uDto(m));
        }
        return rezultat;
    }

    /**
     * Današnja masa (Europe/Zagreb): aktivnost i cilj preuzimaju se iz zadnjeg mjerenja, a postojeće današnje
     * mjerenje se ažurira. Preporuka se zatim računa istom metodom kao kod onboardinga i Postavki ciljeva.
     */
    @Transactional
    public BrzoMjerenjeDTO brzoMjerenje(BrzoMjerenjeRequest request) {

        if (request.getIdKorisnik() == null) {
            throw new IllegalArgumentException("idKorisnik je obavezan.");
        }

        BigDecimal masa = request.getMasa();

        if (masa == null || masa.compareTo(NAJMANJA_MASA) < 0 || masa.compareTo(NAJVECA_MASA) > 0) {
            throw new IllegalArgumentException("Masa mora biti između 20 i 400 kg.");
        }

        if (masa.stripTrailingZeros().scale() > 1) {
            throw new IllegalArgumentException("Masa smije imati najviše jednu decimalu.");
        }

        Mjerenje zadnje = mjerenjeRepository
                .findZadnjaMjerenjaZaKorisnika(request.getIdKorisnik())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nema mjerenje (prvo završi onboarding)."));

        LocalDate danas = LocalDate.now(ObrokService.ZAGREB);

        Mjerenje mjerenje = mjerenjeRepository
                .findFirstByIdKorisnikAndDatumOrderByIdDesc(request.getIdKorisnik(), danas)
                .orElseGet(() -> {
                    Mjerenje novo = new Mjerenje();
                    novo.setIdKorisnik(request.getIdKorisnik());
                    novo.setRazina_aktivnosti(zadnje.getRazina_aktivnosti());
                    novo.setTip_cilja(zadnje.getTip_cilja());
                    novo.setDatum(danas);
                    return novo;
                });

        mjerenje.setMasa(masa.setScale(1));

        Mjerenje spremljeno = mjerenjeRepository.saveAndFlush(mjerenje);

        Preporuka preporuka = izracunPreporukeService.izracunajPreporuku(request.getIdKorisnik());

        return new BrzoMjerenjeDTO(uDto(spremljeno), preporuka);
    }

    private MjerenjeMaseDTO uDto(Mjerenje m) {
        return new MjerenjeMaseDTO(m.getId(), m.getDatum(), m.getMasa());
    }
}
