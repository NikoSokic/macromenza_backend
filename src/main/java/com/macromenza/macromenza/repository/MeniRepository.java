package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.Meni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeniRepository extends JpaRepository<Meni, Long> {

    @Query("""
            SELECT m FROM Meni m
            LEFT JOIN FETCH m.stavke s
            LEFT JOIN FETCH s.jelo
            WHERE (:tip IS NULL OR m.tipObroka = :tip)
              AND (:vegetarijanski IS NULL OR m.vegetarijanski = :vegetarijanski)
            ORDER BY m.tipObroka, m.vegetarijanski, m.redniBroj, s.redoslijed
            """)
    List<Meni> pronadiMenije(
            @Param("tip") String tip,
            @Param("vegetarijanski") Boolean vegetarijanski
    );

    @Query("""
            SELECT m FROM Meni m
            LEFT JOIN FETCH m.stavke s
            LEFT JOIN FETCH s.jelo
            WHERE m.id = :id
            """)
    Optional<Meni> pronadiPoId(@Param("id") Long id);
}
