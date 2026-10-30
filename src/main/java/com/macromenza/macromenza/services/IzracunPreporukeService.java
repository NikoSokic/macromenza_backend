    package com.macromenza.macromenza.services;

    import com.macromenza.macromenza.dto.IzracunDTO;
    import com.macromenza.macromenza.model.Mjerenje;
    import com.macromenza.macromenza.model.Preporuka;
    import com.macromenza.macromenza.model.ProfilPodaci;
    import com.macromenza.macromenza.repository.MjerenjeRepository;
    import com.macromenza.macromenza.repository.PreporukaRepository;
    import com.macromenza.macromenza.repository.ProfilPodaciRepository;

    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.util.Locale;

    @Service
    public class IzracunPreporukeService {

        private final MjerenjeRepository mjerenjeRepository;
        private final ProfilPodaciRepository profilPodaciRepository;
        private final PreporukaRepository preporukaRepository;

        public IzracunPreporukeService(
                MjerenjeRepository mjerenjeRepository,
                ProfilPodaciRepository profilPodaciRepository,
                PreporukaRepository preporukaRepository
        ) {
            this.mjerenjeRepository = mjerenjeRepository;
            this.profilPodaciRepository = profilPodaciRepository;
            this.preporukaRepository = preporukaRepository;
        }

        public Preporuka izracunajPreporuku(Long korisnikId) {

            Mjerenje mjerenje = mjerenjeRepository
                    .findZadnjaMjerenjaZaKorisnika(korisnikId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Korisnik nema mjerenje"));

            ProfilPodaci profil = profilPodaciRepository
                    .findByIdKorisnik(korisnikId)
                    .orElseThrow(() -> new RuntimeException("Korisnik nema profil podatke"));

            IzracunDTO izracun = izracunaj(
                    profil.getSpol(),
                    profil.getDob(),
                    profil.getVisina(),
                    mjerenje.getMasa().doubleValue(),
                    mjerenje.getRazina_aktivnosti(),
                    mjerenje.getTip_cilja()
            );

            Preporuka preporuka = new Preporuka();

            preporuka.setIdKorisnik(mjerenje.getIdKorisnik());
            preporuka.setKalorije(izracun.getCiljKalorije());
            preporuka.setProteini(izracun.getProteini());
            preporuka.setUgljikohidrati(izracun.getUgljikohidrati());
            preporuka.setMasti(izracun.getMasti());
            preporuka.setDatum(LocalDate.now());

            return preporukaRepository.save(preporuka);
        }

        /** Jedini izračun preporuke: koristi ga i spremanje i pregled (POST /api/preporuke/pregled). */
        public IzracunDTO izracunaj(
                String spol,
                int dob,
                int visina,
                double masa,
                String razinaAktivnosti,
                String tipCilja
        ) {
            boolean muski = spol.equalsIgnoreCase("M");
            double bmr = 10 * masa + 6.25 * visina - 5 * dob + (muski ? 5 : -161);

            double faktorAktivnosti = odrediFaktorAktivnosti(razinaAktivnosti);
            double tdee = bmr * faktorAktivnosti;
            double kalorije = prilagodiKalorijePremaCilju(tdee, tipCilja);

            double proteini = masa * 2;
            double masti = (kalorije * 0.25) / 9;
            double ugljikohidrati = (kalorije - proteini * 4 - masti * 9) / 4;

            String formula = "10 × " + broj(masa) + " + 6,25 × " + visina + " − 5 × " + dob
                    + (muski ? " + 5" : " − 161");

            return new IzracunDTO(
                    jednaDecimala(bmr),
                    formula,
                    faktorAktivnosti,
                    jednaDecimala(tdee),
                    jednaDecimala(kalorije - tdee),
                    (int) Math.round(kalorije),
                    (int) Math.round(proteini),
                    (int) Math.round(masti),
                    (int) Math.round(ugljikohidrati)
            );
        }

        private double jednaDecimala(double vrijednost) {
            return Math.round(vrijednost * 10) / 10.0;
        }

        private String broj(double vrijednost) {
            return String.format(Locale.forLanguageTag("hr-HR"), "%.1f", vrijednost).replace(",0", "");
        }

        private double odrediFaktorAktivnosti(String razinaAktivnosti) {
            return switch (razinaAktivnosti.toLowerCase()) {
                case "sjedilacka" -> 1.2;
                case "lagana" -> 1.375;
                case "umjerena" -> 1.55;
                case "visoka" -> 1.725;
                default -> 1.2;
            };
        }

        private double prilagodiKalorijePremaCilju(double kalorije, String tipCilja) {
            return switch (tipCilja.toLowerCase()) {
                case "mrsavljenje" -> kalorije - 500;
                case "dobivanje_mase" -> kalorije + 300;
                case "odrzavanje" -> kalorije;
                default -> kalorije;
            };
        }


    }
