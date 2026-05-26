package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.ProfilPodaci;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilPodaciRepository extends JpaRepository<ProfilPodaci, Long> {

    Optional<ProfilPodaci> findByIdKorisnik(Long idKorisnik);

}