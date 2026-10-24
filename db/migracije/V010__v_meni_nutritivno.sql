-- Korak 4 (3/3): nutritivne vrijednosti stavki menija i totali menija računaju se u bazi.
-- stavka = v_jelo_nutritivno.x * kolicina; meni = SUM(v_jelo_nutritivno.x * kolicina), zaokruženo na 2 decimale.
-- Stavke s priblizno = true ulaze u total.

CREATE VIEW v_meni_stavka_nutritivno WITH (security_invoker = true) AS
SELECT ms.id AS id_meni_stavka,
       ms.id_meni,
       ROUND(v.kalorije       * ms.kolicina, 2) AS kalorije,
       ROUND(v.proteini       * ms.kolicina, 2) AS proteini,
       ROUND(v.ugljikohidrati * ms.kolicina, 2) AS ugljikohidrati,
       ROUND(v.masti          * ms.kolicina, 2) AS masti
FROM meni_stavka ms
JOIN v_jelo_nutritivno v ON v.id_jelo = ms.id_jelo;

CREATE VIEW v_meni_nutritivno WITH (security_invoker = true) AS
SELECT m.id AS id_meni,
       COALESCE(ROUND(SUM(v.kalorije       * ms.kolicina), 2), 0) AS kalorije,
       COALESCE(ROUND(SUM(v.proteini       * ms.kolicina), 2), 0) AS proteini,
       COALESCE(ROUND(SUM(v.ugljikohidrati * ms.kolicina), 2), 0) AS ugljikohidrati,
       COALESCE(ROUND(SUM(v.masti          * ms.kolicina), 2), 0) AS masti
FROM meni m
LEFT JOIN meni_stavka ms ON ms.id_meni = m.id
LEFT JOIN v_jelo_nutritivno v ON v.id_jelo = ms.id_jelo
GROUP BY m.id;

COMMENT ON VIEW v_meni_stavka_nutritivno IS 'Nutritivne vrijednosti stavke menija (jelo x kolicina, okvirne, USDA/EU tablice)';
COMMENT ON VIEW v_meni_nutritivno IS 'Totali menija (zbroj stavki, okvirne vrijednosti, USDA/EU tablice)';

DO $$
DECLARE
    n_meni int;
    n_null int;
    n_stavka int;
    n_neaktivno int;
BEGIN
    SELECT count(*) INTO n_meni FROM v_meni_nutritivno;
    SELECT count(*) INTO n_null FROM v_meni_stavka_nutritivno
        WHERE kalorije IS NULL OR proteini IS NULL OR ugljikohidrati IS NULL OR masti IS NULL;
    SELECT count(*) INTO n_stavka FROM v_meni_stavka_nutritivno;
    SELECT count(*) INTO n_neaktivno FROM meni_stavka ms JOIN jelo j ON j.id = ms.id_jelo WHERE NOT j.aktivno;
    IF n_meni <> 84 OR n_null <> 0 OR n_stavka <> 458 OR n_neaktivno <> 0 THEN
        RAISE EXCEPTION 'V010: menija=% (84), stavki s NULL vrijednostima=% (0), stavki u viewu=% (458), na neaktivnom jelu=% (0)',
            n_meni, n_null, n_stavka, n_neaktivno;
    END IF;
END $$;
