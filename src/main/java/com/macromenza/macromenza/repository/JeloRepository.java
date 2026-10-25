package com.macromenza.macromenza.repository;

import com.macromenza.macromenza.dto.KategorijaJelaDTO;
import com.macromenza.macromenza.model.Jelo;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JeloRepository extends JpaRepository<Jelo, Long> {
    List<Jelo> findByAktivnoTrueOrderByNaziv();

    long countByAktivnoTrue();

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

    /** Aktivna jela s najviše proteina po kaloriji (savjet dana); jela do 100 kcal se preskaču. */
    @Query("""
            select j.id as idJelo, j.naziv as naziv, n.kalorije as kalorije, n.proteini as proteini
            from Jelo j
            join JeloNutritivno n on n.idJelo = j.id
            where j.aktivno = true and n.kalorije > 100
            order by n.proteini / n.kalorije desc, j.naziv
            """)
    List<SavjetJeloRed> findProteinskaJela(Limit limit);

    /** Aktivna jela iz zadanih kategorija s najmanje kalorija (savjet dana). */
    @Query("""
            select j.id as idJelo, j.naziv as naziv, n.kalorije as kalorije, n.proteini as proteini
            from Jelo j
            join JeloNutritivno n on n.idJelo = j.id
            where j.aktivno = true and j.kategorija in :kategorije
            order by n.kalorije, j.naziv
            """)
    List<SavjetJeloRed> findLaganaJela(@Param("kategorije") Collection<String> kategorije, Limit limit);

    interface SavjetJeloRed {
        Long getIdJelo();
        String getNaziv();
        BigDecimal getKalorije();
        BigDecimal getProteini();
    }

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
