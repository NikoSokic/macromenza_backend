package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Obrok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ObrokRepository extends JpaRepository<Obrok, Long> {

    List<Obrok> findByIdKorisnikAndDatumOrderByVrijemeAscIdAsc(Long idKorisnik, LocalDate datum);

    List<Obrok> findByIdKorisnikAndDatumBetween(Long idKorisnik, LocalDate datumOd, LocalDate datumDo);

    @Query("select distinct o.datum from Obrok o where o.idKorisnik = :idKorisnik order by o.datum desc")
    List<LocalDate> findDatumiZaKorisnika(@Param("idKorisnik") Long idKorisnik);
}
