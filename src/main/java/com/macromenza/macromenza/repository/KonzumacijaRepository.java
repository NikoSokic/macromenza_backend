package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Konzumacija;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KonzumacijaRepository extends JpaRepository<Konzumacija, Long> {

    List<Konzumacija> findByIdKorisnik(Long idKorisnik);
}