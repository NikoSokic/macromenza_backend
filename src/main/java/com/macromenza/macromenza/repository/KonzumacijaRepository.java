package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Konzumacija;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;


public interface KonzumacijaRepository extends JpaRepository<Konzumacija, Long> {

    List<Konzumacija> findByIdKorisnik(Long idKorisnik);
    List<Konzumacija> findByIdKorisnikAndDatum(Long idKorisnik, LocalDate datum);
    List<Konzumacija> findByIdKorisnikOrderByDatumDesc(Long idKorisnik);
    List<Konzumacija> findByIdKorisnikAndDatumBetween(Long idKorisnik, LocalDate datumOd, LocalDate datumDo);
    


}