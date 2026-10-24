-- Korak 3 (4/4): stare konzumacije prelaze na nova jela, stara jela se deaktiviraju (ništa se ne briše).
-- Mapiranje (stari jelo.id -> sifra novog jela):
--   1 -> 72 Pileći file na žaru, 2 -> 73 Lazanje od povrća, 3 -> 17 Kotlet na roštilju,
--   4 -> 0 Juha od graška, 5 -> 1 Juha od kosti, 6 -> 2 Juha od krumpira

UPDATE konzumacija k
SET id_jelo = j.id
FROM (VALUES (1, 72), (2, 73), (3, 17), (4, 0), (5, 1), (6, 2)) AS m (stari_id, sifra)
JOIN jelo j ON j.sifra = m.sifra
WHERE k.id_jelo = m.stari_id;

UPDATE jelo SET aktivno = false WHERE id IN (1, 2, 3, 4, 5, 6) AND sifra IS NULL;

DO $$
DECLARE
    n_konz int;
    n_neaktivno int;
    n_aktivno int;
BEGIN
    SELECT count(*) INTO n_konz FROM konzumacija;
    SELECT count(*) INTO n_neaktivno FROM konzumacija k JOIN jelo j ON j.id = k.id_jelo WHERE NOT j.aktivno;
    SELECT count(*) INTO n_aktivno FROM jelo WHERE aktivno;
    IF n_konz <> 70 OR n_neaktivno <> 0 OR n_aktivno <> 311 THEN
        RAISE EXCEPTION 'V007: konzumacija=% (ocekivano 70), na neaktivnom jelu=% (0), aktivnih jela=% (311)',
            n_konz, n_neaktivno, n_aktivno;
    END IF;
END $$;
