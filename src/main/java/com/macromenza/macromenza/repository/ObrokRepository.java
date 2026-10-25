package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Obrok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObrokRepository extends JpaRepository<Obrok, Long> {

    Optional<Obrok> findByIdAndIdKorisnik(Long id, Long idKorisnik);

    List<Obrok> findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(Long idKorisnik, LocalDate datum);

    List<Obrok> findByIdKorisnikAndDatumBetween(Long idKorisnik, LocalDate datumOd, LocalDate datumDo);

    @Query("select distinct o.datum from Obrok o where o.idKorisnik = :idKorisnik order by o.datum desc")
    List<LocalDate> findDatumiZaKorisnika(@Param("idKorisnik") Long idKorisnik);
}
