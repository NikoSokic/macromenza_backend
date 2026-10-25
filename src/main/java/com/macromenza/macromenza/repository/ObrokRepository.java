package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Obrok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObrokRepository extends JpaRepository<Obrok, Long> {

    Optional<Obrok> findByIdAndIdKorisnik(Long id, Long idKorisnik);

    List<Obrok> findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(Long idKorisnik, LocalDate datum);

    List<Obrok> findByIdKorisnikAndDatumBetween(Long idKorisnik, LocalDate datumOd, LocalDate datumDo);

    @Query("select distinct o.datum from Obrok o where o.idKorisnik = :idKorisnik order by o.datum desc")
    List<LocalDate> findDatumiZaKorisnika(@Param("idKorisnik") Long idKorisnik);

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

    interface TipObrokaRed {
        String getTipObroka();
        Long getBrojObroka();
        BigDecimal getKalorije();
        BigDecimal getProteini();
        BigDecimal getUgljikohidrati();
        BigDecimal getMasti();
    }
}
