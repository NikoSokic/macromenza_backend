package com.macromenza.macromenza.services;

import com.macromenza.macromenza.dto.DanPregledDTO;
import com.macromenza.macromenza.dto.NutritivneVrijednostiDTO;
import com.macromenza.macromenza.repository.JeloRepository;
import com.macromenza.macromenza.repository.JeloRepository.SavjetJeloRed;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SavjetService {

    private static final LocalTime POSLIJE_PODNE = LocalTime.of(15, 0);
    private static final LocalTime VECER_VRIJEME = LocalTime.of(19, 0);
    private static final BigDecimal POLA = new BigDecimal("0.5");
    private static final BigDecimal PREKO_CILJA = new BigDecimal("1.1");
    private static final List<String> LAGANE_KATEGORIJE = List.of("Salate", "Juhe", "Voće");
    private static final int BROJ_JELA = 3;

    /** Opći savjeti vezani uz menzu; jedan po danu u godini. */
    private static final List<String> OPCI_SAVJETI = List.of(
            "Juha prije glavnog jela brzo zasiti i obično ima malo kalorija.",
            "Uz glavno jelo uzmi salatu umjesto dodatnog priloga od krumpira ili tjestenine.",
            "Voda ili čaj umjesto soka štede i do 100 kcal po obroku.",
            "Umaci i prelivi lako dodaju stotinjak kcal. Uzmi ih manje ili sa strane.",
            "Pohana jela upijaju dosta ulja. Pečena ili kuhana jela imaju manje masti.",
            "Probaj vegetarijansko jelo: često je lakše, a proteine daju grah, jaja ili sir.",
            "Kruh uz obrok se brzo zbroji. Jedna kriška je oko 70 kcal.",
            "Ako si jako gladan, počni salatom ili juhom pa tek onda glavnim jelom.",
            "Proteini u svakom obroku pomažu da dulje ostaneš sit. Dodaj jaje, sir ili meso.",
            "Voće za užinu je jednostavan način da dođeš do vlakana i vitamina.",
            "Porcije možeš unositi po pola: ako ne pojedeš sve, upiši 0,5.",
            "Najveći obrok najčešće je ručak. Za večeru izaberi nešto lakše.",
            "Deserti iz menze lako dosegnu 300 kcal. Podijeli ga ili ostavi za rjeđe prilike.",
            "Zapiši obrok čim ga pojedeš. Kasnije se teško sjetiš porcija i priloga.",
            "Vrijednosti u aplikaciji su okvirne, pa prati trend kroz tjedan, a ne jedan dan."
    );

    private final JeloRepository jeloRepository;

    public SavjetService(JeloRepository jeloRepository) {
        this.jeloRepository = jeloRepository;
    }

    /**
     * Prvo pravilo koje vrijedi: (1) poslijepodne premalo proteina, (2) večer previše kalorija,
     * (3) opći savjet po danu u godini. Vremenska pravila vrijede samo za današnji dan.
     */
    public DanPregledDTO.Savjet odaberi(
            NutritivneVrijednostiDTO cilj,
            NutritivneVrijednostiDTO pojedeno,
            LocalDate datum,
            LocalDate danas,
            LocalTime vrijeme
    ) {
        boolean jeDanas = datum.equals(danas);

        if (cilj != null && jeDanas) {

            if (!vrijeme.isBefore(POSLIJE_PODNE)
                    && pojedeno.getProteini().compareTo(cilj.getProteini().multiply(POLA)) < 0) {

                return new DanPregledDTO.Savjet(
                        "Nedostaje ti proteina",
                        "Pojeo si manje od pola dnevnog cilja proteina. Ova jela imaju najviše proteina po kaloriji.",
                        uJela(jeloRepository.findProteinskaJela(Limit.of(BROJ_JELA)))
                );
            }

            if (!vrijeme.isBefore(VECER_VRIJEME)
                    && pojedeno.getKalorije().compareTo(cilj.getKalorije().multiply(PREKO_CILJA)) > 0) {

                return new DanPregledDTO.Savjet(
                        "Laganija večera",
                        "Već si iznad dnevnog cilja. Za večeru izaberi nešto lagano.",
                        uJela(jeloRepository.findLaganaJela(LAGANE_KATEGORIJE, Limit.of(BROJ_JELA)))
                );
            }
        }

        return new DanPregledDTO.Savjet(
                "Savjet za menzu",
                OPCI_SAVJETI.get(datum.getDayOfYear() % OPCI_SAVJETI.size()),
                List.of()
        );
    }

    private List<DanPregledDTO.SavjetJelo> uJela(List<SavjetJeloRed> redovi) {
        return redovi.stream()
                .map(r -> new DanPregledDTO.SavjetJelo(r.getIdJelo(), r.getNaziv(), r.getKalorije(), r.getProteini()))
                .toList();
    }
}
