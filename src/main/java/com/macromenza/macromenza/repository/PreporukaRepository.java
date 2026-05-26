package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Preporuka;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreporukaRepository extends JpaRepository<Preporuka, Long> {

    List<Preporuka> findByIdKorisnik(Long idKorisnik);

    Optional<Preporuka> findTopByIdKorisnikOrderByIdDesc(Long idKorisnik);
}