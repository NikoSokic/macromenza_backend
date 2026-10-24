package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Konzumacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;


public interface KonzumacijaRepository extends JpaRepository<Konzumacija, Long> {

    List<Konzumacija> findByIdKorisnik(Long idKorisnik);
    List<Konzumacija> findByIdObrokIn(Collection<Long> idObroka);
    List<Konzumacija> findByIdObrokOrderByIdAsc(Long idObrok);

    @Query("""
            select k from Konzumacija k join Obrok o on o.id = k.idObrok
            where o.idKorisnik = :idKorisnik and o.datum = :datum
            order by o.vrijeme, k.id
            """)
    List<Konzumacija> findZaDan(@Param("idKorisnik") Long idKorisnik, @Param("datum") LocalDate datum);

    @Query("""
            select k.idObrok as idObrok, k.idJelo as idJelo, j.naziv as naziv, k.kolicina as kolicina,
                   n.kalorije as kalorije, n.proteini as proteini,
                   n.ugljikohidrati as ugljikohidrati, n.masti as masti
            from Konzumacija k
            join Jelo j on j.id = k.idJelo
            join JeloNutritivno n on n.idJelo = k.idJelo
            where k.idObrok in :idObroka
            order by k.idObrok, k.id
            """)
    List<StavkaObrokaRed> findStavkeObroka(@Param("idObroka") Collection<Long> idObroka);

    /** Red stavke obroka: vrijednosti jela su po porciji (v_jelo_nutritivno), još nepomnožene s kolicinom. */
    interface StavkaObrokaRed {
        Long getIdObrok();
        Long getIdJelo();
        String getNaziv();
        BigDecimal getKolicina();
        BigDecimal getKalorije();
        BigDecimal getProteini();
        BigDecimal getUgljikohidrati();
        BigDecimal getMasti();
    }
}
