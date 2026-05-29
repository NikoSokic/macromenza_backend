package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    Optional<Korisnik> findBySupabaseUid(UUID supabaseUid);

}