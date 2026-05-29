package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Mjerenje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MjerenjeRepository extends JpaRepository<Mjerenje, Long> {

    List<Mjerenje> findByIdKorisnik(Long idKorisnik);

    Optional<Mjerenje> findTopByIdKorisnikOrderByDatumDesc(Long idKorisnik);

    Optional<Mjerenje> findTopByIdKorisnikOrderByIdDesc(Long idKorisnik);

    @Query("SELECT m FROM Mjerenje m WHERE m.idKorisnik = :idKorisnik ORDER BY m.id DESC")
    List<Mjerenje> findZadnjaMjerenjaZaKorisnika(@Param("idKorisnik") Long idKorisnik);

}