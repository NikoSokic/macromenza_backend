package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Obrok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObrokRepository extends JpaRepository<Obrok, Long> {

    Optional<Obrok> findByIdAndIdKorisnik(Long id, Long idKorisnik);

    List<Obrok> findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(Long idKorisnik, LocalDate datum);

    boolean existsByIdKorisnikAndDatum(Long idKorisnik, LocalDate datum);

    /** Bulk brisanje; konzumacije briše baza (ON DELETE CASCADE). */
    @Modifying
    @Query("delete from Obrok o where o.idKorisnik = :idKorisnik")
    int obrisiSveZaKorisnika(@Param("idKorisnik") Long idKorisnik);

    @Modifying
    @Query("delete from Obrok o where o.idKorisnik = :idKorisnik and o.izvor = :izvor")
    int obrisiPoIzvoru(@Param("idKorisnik") Long idKorisnik, @Param("izvor") String izvor);

    /** Totali dana po tipu obroka (v_jelo_nutritivno × kolicina); jedan upit. Tipovi bez obroka nisu u rezultatu. */
    @Query("""
            select o.tipObroka as tipObroka, count(distinct o.id) as brojObroka,
                   sum(n.kalorije * k.kolicina) as kalorije, sum(n.proteini * k.kolicina) as proteini,
                   sum(n.ugljikohidrati * k.kolicina) as ugljikohidrati, sum(n.masti * k.kolicina) as masti
            from Obrok o
            join Konzumacija k on k.idObrok = o.id
            join JeloNutritivno n on n.idJelo = k.idJelo
            where o.idKorisnik = :idKorisnik and o.datum = :datum
            group by o.tipObroka
            """)
    List<TipObrokaRed> findTotaliPoTipu(@Param("idKorisnik") Long idKorisnik, @Param("datum") LocalDate datum);

    /**
     * Sedam dana od [pocetak] (generate_series) s totalima i ciljem dana (zadnja preporuka s datumom <= dan); jedan
     * upit. Stavke se zaokružuju na 2 decimale prije zbrajanja, kao u GET /api/obroci. Dan bez unosa ima null totale.
     * [imaPrethodni]: postoji li obrok prije [pocetak] (isti u svakom redu).
     */
    @Query(value = """
            WITH dani AS (
                SELECT CAST(:pocetak AS date) + g.i AS datum
                FROM generate_series(0, 6) AS g(i)
            ),
            totali AS (
                SELECT o.datum,
                       SUM(ROUND(n.kalorije * k.kolicina, 2))       AS kalorije,
                       SUM(ROUND(n.proteini * k.kolicina, 2))       AS proteini,
                       SUM(ROUND(n.ugljikohidrati * k.kolicina, 2)) AS ugljikohidrati,
                       SUM(ROUND(n.masti * k.kolicina, 2))          AS masti
                FROM obrok o
                JOIN konzumacija k ON k.id_obrok = o.id
                JOIN v_jelo_nutritivno n ON n.id_jelo = k.id_jelo
                WHERE o.id_korisnik = :idKorisnik
                  AND o.datum BETWEEN CAST(:pocetak AS date) AND CAST(:pocetak AS date) + 6
                GROUP BY o.datum
            )
            SELECT d.datum           AS "datum",
                   t.kalorije        AS "kalorije",
                   t.proteini        AS "proteini",
                   t.ugljikohidrati  AS "ugljikohidrati",
                   t.masti           AS "masti",
                   p.kalorije        AS "ciljKalorije",
                   p.proteini        AS "ciljProteini",
                   p.ugljikohidrati  AS "ciljUgljikohidrati",
                   p.masti           AS "ciljMasti",
                   EXISTS (
                       SELECT 1 FROM obrok op
                       WHERE op.id_korisnik = :idKorisnik AND op.datum < CAST(:pocetak AS date)
                   )                 AS "imaPrethodni",
                   EXISTS (
                       SELECT 1 FROM obrok opr
                       WHERE opr.id_korisnik = :idKorisnik AND opr.izvor = 'PRIMJER'
                         AND opr.datum BETWEEN CAST(:pocetak AS date) AND CAST(:pocetak AS date) + 6
                   )                 AS "imaPrimjer"
            FROM dani d
            LEFT JOIN totali t ON t.datum = d.datum
            LEFT JOIN LATERAL (
                SELECT pr.kalorije, pr.proteini, pr.ugljikohidrati, pr.masti
                FROM preporuka pr
                WHERE pr.id_korisnik = :idKorisnik AND pr.datum <= d.datum
                ORDER BY pr.datum DESC, pr.id DESC
                LIMIT 1
            ) p ON true
            ORDER BY d.datum
            """, nativeQuery = true)
    List<TjedanDanRed> findTjedan(@Param("idKorisnik") Long idKorisnik, @Param("pocetak") LocalDate pocetak);

    interface TjedanDanRed {
        LocalDate getDatum();
        BigDecimal getKalorije();
        BigDecimal getProteini();
        BigDecimal getUgljikohidrati();
        BigDecimal getMasti();
        Integer getCiljKalorije();
        Integer getCiljProteini();
        Integer getCiljUgljikohidrati();
        Integer getCiljMasti();
        Boolean getImaPrethodni();
        Boolean getImaPrimjer();
    }

    interface TipObrokaRed {
        String getTipObroka();
        Long getBrojObroka();
        BigDecimal getKalorije();
        BigDecimal getProteini();
        BigDecimal getUgljikohidrati();
        BigDecimal getMasti();
    }
}
