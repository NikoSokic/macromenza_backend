-- Korak 7: nutritivne vrijednosti svakog sastojka jela (za detalj jela).
-- vrijednost = namirnica.x * grami / 100, zaokruženo na 2 decimale po sastojku.
-- Stara jela bez sastojaka nemaju redove u ovom viewu.

CREATE VIEW v_jelo_sastojak_nutritivno WITH (security_invoker = true) AS
SELECT s.id_jelo,
       s.id AS id_jelo_sastojak,
       s.naziv_u_recepturi,
       n.kljuc AS namirnica_kljuc,
       s.grami,
       ROUND(n.kalorije       * s.grami / 100, 2) AS kalorije,
       ROUND(n.proteini       * s.grami / 100, 2) AS proteini,
       ROUND(n.ugljikohidrati * s.grami / 100, 2) AS ugljikohidrati,
       ROUND(n.masti          * s.grami / 100, 2) AS masti
FROM jelo_sastojak s
JOIN namirnica n ON n.id = s.id_namirnica;

COMMENT ON VIEW v_jelo_sastojak_nutritivno IS 'Nutritivne vrijednosti sastojka jela u jednoj porciji (okvirne, USDA/EU tablice), iz jelo_sastojak x namirnica';

DO $$
DECLARE
    n_redova int;
    n_sastojaka int;
    n_jela int;
    n_odstupanja int;
BEGIN
    SELECT count(*) INTO n_redova FROM v_jelo_sastojak_nutritivno;
    SELECT count(*) INTO n_sastojaka FROM jelo_sastojak;
    SELECT count(DISTINCT id_jelo) INTO n_jela FROM v_jelo_sastojak_nutritivno;
    SELECT count(*) INTO n_odstupanja
    FROM (
        SELECT s.id_jelo,
               SUM(s.kalorije) AS kalorije, SUM(s.proteini) AS proteini,
               SUM(s.ugljikohidrati) AS ugljikohidrati, SUM(s.masti) AS masti
        FROM v_jelo_sastojak_nutritivno s
        GROUP BY s.id_jelo
    ) z
    JOIN v_jelo_nutritivno v ON v.id_jelo = z.id_jelo
    WHERE abs(z.kalorije       - v.kalorije)       > 0.05
       OR abs(z.proteini       - v.proteini)       > 0.05
       OR abs(z.ugljikohidrati - v.ugljikohidrati) > 0.05
       OR abs(z.masti          - v.masti)          > 0.05;
    IF n_redova <> n_sastojaka OR n_jela <> 311 OR n_odstupanja <> 0 THEN
        RAISE EXCEPTION 'V013: redova u viewu=% (jelo_sastojak=%), jela sa sastojcima=% (311), jela s odstupanjem > 0,05=% (0)',
            n_redova, n_sastojaka, n_jela, n_odstupanja;
    END IF;
END $$;
