package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.model.JeloSastojakNutritivno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JeloSastojakNutritivnoRepository extends JpaRepository<JeloSastojakNutritivno, Long> {
    List<JeloSastojakNutritivno> findByIdJeloOrderByGramiDescIdJeloSastojakAsc(Long idJelo);
}
