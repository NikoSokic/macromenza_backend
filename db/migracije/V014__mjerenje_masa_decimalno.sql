-- Korak 14: masa s jednom decimalom (integer -> NUMERIC(5,1)), raspon 20–400 kg, indeks za graf mase.
-- Backup: mjerenje_backup_k14.
-- UNIQUE (id_korisnik, datum) se ne dodaje: postojeći unosi (onboarding, Postavke ciljeva) dodaju novi red,
-- a brzo mjerenje ažurira zadnje današnje. Duplikati prije V014: korisnik 1 / 2026-05-29 (6), 10 / 2026-06-02 (2), 15 / 2026-08-12 (2).

CREATE TABLE mjerenje_backup_k14 AS SELECT * FROM mjerenje;

ALTER TABLE mjerenje ALTER COLUMN masa TYPE NUMERIC(5,1) USING masa::numeric;
ALTER TABLE mjerenje ADD CONSTRAINT mjerenje_masa_raspon CHECK (masa BETWEEN 20 AND 400);
CREATE INDEX mjerenje_korisnik_datum_idx ON mjerenje (id_korisnik, datum);

DO $$
DECLARE
    n int;
    n_backup int;
    n_promijenjeno int;
BEGIN
    SELECT count(*) INTO n FROM mjerenje;
    SELECT count(*) INTO n_backup FROM mjerenje_backup_k14;
    SELECT count(*) INTO n_promijenjeno FROM mjerenje m JOIN mjerenje_backup_k14 b ON b.id = m.id
        WHERE m.masa IS DISTINCT FROM b.masa::numeric;
    IF n <> 21 OR n_backup <> 21 OR n_promijenjeno <> 0 THEN
        RAISE EXCEPTION 'V014: mjerenje=% (21), backup=% (21), promijenjena masa=% (0)', n, n_backup, n_promijenjeno;
    END IF;
END $$;

SELECT (SELECT count(*) FROM mjerenje) AS mjerenja,
       (SELECT count(*) FROM mjerenje_backup_k14) AS backup,
       (SELECT data_type || '(' || numeric_precision || ',' || numeric_scale || ')'
        FROM information_schema.columns WHERE table_name = 'mjerenje' AND column_name = 'masa') AS tip_mase;

SELECT id_korisnik, datum, count(*) AS broj FROM mjerenje GROUP BY 1, 2 HAVING count(*) > 1 ORDER BY 1, 2;
