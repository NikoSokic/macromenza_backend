package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Preporuka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PreporukaRepository extends JpaRepository<Preporuka, Long> {

    List<Preporuka> findByIdKorisnik(Long idKorisnik);

    @Modifying
    @Query("delete from Preporuka p where p.idKorisnik = :idKorisnik")
    int obrisiSveZaKorisnika(@Param("idKorisnik") Long idKorisnik);

    Optional<Preporuka> findTopByIdKorisnikOrderByIdDesc(Long idKorisnik);

    /** Zadnja preporuka važeća za zadani datum (datum preporuke <= datum); bez automatskog izračuna. */
    Optional<Preporuka> findTopByIdKorisnikAndDatumLessThanEqualOrderByDatumDescIdDesc(Long idKorisnik, LocalDate datum);
}