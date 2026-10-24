-- Korak 3 (3/4): nutritivne vrijednosti jela računaju se u bazi.
-- vrijednost = SUM(namirnica.x * grami / 100), zaokružen zbroj na 2 decimale.
-- Jela bez sastojaka (6 starih): COALESCE na vrijednosti iz tablice jelo, masa_g je NULL.

CREATE VIEW v_jelo_nutritivno WITH (security_invoker = true) AS
SELECT j.id AS id_jelo,
       COALESCE(ROUND(SUM(n.kalorije       * s.grami / 100), 2), j.kalorije)       AS kalorije,
       COALESCE(ROUND(SUM(n.proteini       * s.grami / 100), 2), j.proteini)       AS proteini,
       COALESCE(ROUND(SUM(n.ugljikohidrati * s.grami / 100), 2), j.ugljikohidrati) AS ugljikohidrati,
       COALESCE(ROUND(SUM(n.masti          * s.grami / 100), 2), j.masti)          AS masti,
       ROUND(SUM(s.grami), 2)                                                      AS masa_g
FROM jelo j
LEFT JOIN jelo_sastojak s ON s.id_jelo = j.id
LEFT JOIN namirnica n ON n.id = s.id_namirnica
GROUP BY j.id;

COMMENT ON VIEW v_jelo_nutritivno IS 'Nutritivne vrijednosti jela po porciji (okvirne, USDA/EU tablice), izračun iz jelo_sastojak x namirnica';
