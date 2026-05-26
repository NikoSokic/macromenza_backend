package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {
}