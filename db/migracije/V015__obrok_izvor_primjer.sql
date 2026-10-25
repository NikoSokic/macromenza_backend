-- Korak 15: obrok.izvor smije biti i 'PRIMJER' (primjer tjedna u Povijesti).
-- Podaci se ne mijenjaju, samo se proširuje CHECK; zato backup tablica nije potrebna.

DO $$
DECLARE
    n_prije int;
    n_poslije int;
    n_neispravnih int;
    ime text;
BEGIN
    SELECT count(*) INTO n_prije FROM obrok;

    SELECT conname INTO ime
    FROM pg_constraint
    WHERE conrelid = 'obrok'::regclass AND contype = 'c' AND pg_get_constraintdef(oid) LIKE '%izvor%';

    IF ime IS NOT NULL THEN
        EXECUTE format('ALTER TABLE obrok DROP CONSTRAINT %I', ime);
    END IF;

    ALTER TABLE obrok
        ADD CONSTRAINT obrok_izvor_check CHECK (izvor IN ('RUCNO', 'MENI', 'SLIKA', 'OPIS', 'PRIMJER'));

    SELECT count(*) INTO n_poslije FROM obrok;
    SELECT count(*) INTO n_neispravnih FROM obrok
        WHERE izvor NOT IN ('RUCNO', 'MENI', 'SLIKA', 'OPIS', 'PRIMJER');

    IF n_prije <> n_poslije OR n_neispravnih <> 0 THEN
        RAISE EXCEPTION 'V015: obroka prije=%, poslije=%, neispravnih izvora=%', n_prije, n_poslije, n_neispravnih;
    END IF;
END $$;

COMMENT ON COLUMN obrok.izvor IS 'RUCNO, MENI, SLIKA, OPIS ili PRIMJER (primjer tjedna, briše se zasebno)';

SELECT izvor, count(*) AS broj FROM obrok GROUP BY izvor ORDER BY izvor;
