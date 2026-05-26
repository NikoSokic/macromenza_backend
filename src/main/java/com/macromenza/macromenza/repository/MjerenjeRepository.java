package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Mjerenje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MjerenjeRepository extends JpaRepository<Mjerenje, Long> {

    List<Mjerenje> findByIdKorisnik(Long idKorisnik);

    Optional<Mjerenje> findTopByIdKorisnikOrderByDatumDesc(Long idKorisnik);

}