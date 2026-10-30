-- Korak 19: lozinke čuva Supabase Auth, pa korisnik.lozinka_hash više ne treba.
-- Prije brisanja stupca radi se backup cijele tablice (vrijednosti hasheva ne čitaju se ni ne ispisuju).

DO $$
DECLARE
    n_prije int;
    n_backup int;
    n_poslije int;
BEGIN
    SELECT count(*) INTO n_prije FROM korisnik;

    IF to_regclass('korisnik_backup_k19') IS NULL THEN
        CREATE TABLE korisnik_backup_k19 AS SELECT * FROM korisnik;
    END IF;
    SELECT count(*) INTO n_backup FROM korisnik_backup_k19;

    IF n_backup < n_prije THEN
        RAISE EXCEPTION 'V016: backup ima % redaka, a korisnik %', n_backup, n_prije;
    END IF;

    ALTER TABLE korisnik DROP COLUMN IF EXISTS lozinka_hash;

    SELECT count(*) INTO n_poslije FROM korisnik;
    IF n_poslije <> n_prije THEN
        RAISE EXCEPTION 'V016: korisnika prije=%, poslije=%', n_prije, n_poslije;
    END IF;
END $$;
