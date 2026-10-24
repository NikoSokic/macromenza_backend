-- Korak 5 (2/2): postojeće konzumacije dobivaju obrok (jedan po korisniku, datumu i tipu obroka).
-- Vrijeme starih obroka je zadano po tipu (stvarno vrijeme nije zapisano).
-- konzumacija.datum i tip_obroka ostaju i dalje se popunjavaju iz obroka. Backup: konzumacija_backup_k5 (V011).

UPDATE konzumacija SET tip_obroka = upper(tip_obroka) WHERE tip_obroka <> upper(tip_obroka);

-- Nepoznat tip daje NULL vrijeme, pa INSERT pada (vrijeme je NOT NULL).
INSERT INTO obrok (id_korisnik, datum, vrijeme, tip_obroka, izvor)
SELECT DISTINCT k.id_korisnik,
       k.datum,
       CASE k.tip_obroka
           WHEN 'DORUCAK' THEN time '08:00'
           WHEN 'RUCAK'   THEN time '13:00'
           WHEN 'UZINA'   THEN time '16:00'
           WHEN 'VECERA'  THEN time '19:00'
       END,
       k.tip_obroka,
       'RUCNO'
FROM konzumacija k
WHERE k.id_obrok IS NULL;

UPDATE konzumacija k
SET id_obrok = o.id
FROM obrok o
WHERE k.id_obrok IS NULL
  AND o.id_korisnik = k.id_korisnik
  AND o.datum = k.datum
  AND o.tip_obroka = k.tip_obroka;

DO $$
DECLARE
    n_konz int;
    n_bez_obroka int;
    n_prazni_obroci int;
    n_mala_slova int;
    n_neuskladeno int;
BEGIN
    SELECT count(*) INTO n_konz FROM konzumacija;
    SELECT count(*) INTO n_bez_obroka FROM konzumacija WHERE id_obrok IS NULL;
    SELECT count(*) INTO n_prazni_obroci FROM obrok o
        WHERE NOT EXISTS (SELECT 1 FROM konzumacija k WHERE k.id_obrok = o.id);
    SELECT count(*) INTO n_mala_slova FROM konzumacija WHERE tip_obroka <> upper(tip_obroka);
    SELECT count(*) INTO n_neuskladeno FROM konzumacija k JOIN obrok o ON o.id = k.id_obrok
        WHERE k.id_korisnik <> o.id_korisnik OR k.datum <> o.datum OR k.tip_obroka <> o.tip_obroka;
    IF n_konz <> 70 OR n_bez_obroka <> 0 OR n_prazni_obroci <> 0 OR n_mala_slova <> 0 OR n_neuskladeno <> 0 THEN
        RAISE EXCEPTION 'V012: konzumacija=% (70), bez obroka=% (0), obroka bez konzumacije=% (0), malim slovima=% (0), neuskladeno s obrokom=% (0)',
            n_konz, n_bez_obroka, n_prazni_obroci, n_mala_slova, n_neuskladeno;
    END IF;
END $$;

ALTER TABLE konzumacija ALTER COLUMN id_obrok SET NOT NULL;

SELECT (SELECT count(*) FROM obrok) AS obroka,
       (SELECT count(*) FROM konzumacija) AS konzumacija,
       (SELECT count(*) FROM konzumacija_backup_k5) AS backup;
