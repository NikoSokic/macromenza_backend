package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Jelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JeloRepository extends JpaRepository<Jelo, Long> {
    List<Jelo> findByNazivContainingIgnoreCase(String naziv);

}