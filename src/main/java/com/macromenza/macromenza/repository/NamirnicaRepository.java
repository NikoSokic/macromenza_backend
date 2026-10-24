package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Namirnica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NamirnicaRepository extends JpaRepository<Namirnica, Long> {
    List<Namirnica> findAllByOrderByKljuc();

    List<Namirnica> findByKljucContainingIgnoreCaseOrderByKljuc(String kljuc);
}
