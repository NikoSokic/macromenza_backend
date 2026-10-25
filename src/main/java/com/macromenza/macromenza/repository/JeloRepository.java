package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.dto.KategorijaJelaDTO;
import com.macromenza.macromenza.model.Jelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JeloRepository extends JpaRepository<Jelo, Long> {
    List<Jelo> findByAktivnoTrueOrderByNaziv();

    List<Jelo> findByAktivnoTrueAndNazivContainingIgnoreCaseOrderByNaziv(String naziv);

    @Query("""
            select new com.macromenza.macromenza.dto.KategorijaJelaDTO(j.kategorija, count(j))
            from Jelo j
            where j.aktivno = true and j.kategorija is not null
            group by j.kategorija
            order by j.kategorija
            """)
    List<KategorijaJelaDTO> findKategorije();

    /** Jelo s vrijednostima i masom porcije iz v_jelo_nutritivno; i za neaktivna jela. */
    @Query("""
            select j.id as idJelo, j.naziv as naziv, j.opis as opis, j.sifra as sifra,
                   j.kategorija as kategorija, j.cijenaEur as cijenaEur, j.napomena as napomena,
                   n.kalorije as kalorije, n.proteini as proteini,
                   n.ugljikohidrati as ugljikohidrati, n.masti as masti, n.masaG as masaG
            from Jelo j
            join JeloNutritivno n on n.idJelo = j.id
            where j.id = :id
            """)
    Optional<JeloDetaljRed> findDetalj(@Param("id") Long id);

    interface JeloDetaljRed {
        Long getIdJelo();
        String getNaziv();
        String getOpis();
        Integer getSifra();
        String getKategorija();
        BigDecimal getCijenaEur();
        String getNapomena();
        BigDecimal getKalorije();
        BigDecimal getProteini();
        BigDecimal getUgljikohidrati();
        BigDecimal getMasti();
        BigDecimal getMasaG();
    }
}
