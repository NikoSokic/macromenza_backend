-- V017: čišćenje baze, brisanje 11 backup tablica.
-- Brisanje je izričito potvrdio Niko (11. 11. 2026).
-- Tablice su nastale kao osiguranje prije migracija (koraci 1, 3, 5, 14 i 19); aplikacija ih ne koristi,
-- nemaju stranih ključeva i ni jedan view ne ovisi o njima.
-- korisnik_backup_k19 i korisnik_backup_brisanje_k1 još sadrže stari stupac lozinka_hash.
-- Stara jela (id 1-6, aktivno = false) namjerno OSTAJU u tablici jelo.

DO $$
DECLARE
    t text;
    tablice text[] := ARRAY[
        'jelo_backup_k3',
        'konzumacija_backup_k3',
        'konzumacija_backup_k5',
        'mjerenje_backup_k14',
        'korisnik_backup_k19',
        'korisnik_backup_brisanje_k1',
        'profil_podaci_backup_brisanje_k1',
        'mjerenje_backup_brisanje_k1',
        'preporuka_backup_brisanje_k1',
        'obrok_backup_brisanje_k1',
        'konzumacija_backup_brisanje_k1'
    ];
    broj int;
BEGIN
    FOREACH t IN ARRAY tablice LOOP
        IF to_regclass('public.' || t) IS NULL THEN
            RAISE EXCEPTION 'V017: tablica % ne postoji, prekidam', t;
        END IF;
    END LOOP;

    SELECT count(*) INTO broj FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '%backup%';
    IF broj <> 11 THEN
        RAISE EXCEPTION 'V017: očekivano 11 backup tablica, nađeno %', broj;
    END IF;

    SELECT count(*) INTO broj FROM jelo WHERE id BETWEEN 1 AND 6 AND aktivno = false;
    IF broj <> 6 THEN
        RAISE EXCEPTION 'V017: očekivano 6 starih jela, nađeno %', broj;
    END IF;
END $$;

-- Bez CASCADE: ako bilo što ipak ovisi o nekoj tablici, DROP pada i cijela migracija se poništava.
DROP TABLE
    public.jelo_backup_k3,
    public.konzumacija_backup_k3,
    public.konzumacija_backup_k5,
    public.mjerenje_backup_k14,
    public.korisnik_backup_k19,
    public.korisnik_backup_brisanje_k1,
    public.profil_podaci_backup_brisanje_k1,
    public.mjerenje_backup_brisanje_k1,
    public.preporuka_backup_brisanje_k1,
    public.obrok_backup_brisanje_k1,
    public.konzumacija_backup_brisanje_k1;

DO $$
DECLARE
    broj int;
BEGIN
    SELECT count(*) INTO broj FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE '%backup%';
    IF broj <> 0 THEN
        RAISE EXCEPTION 'V017: nakon brisanja ostalo je % backup tablica', broj;
    END IF;

    SELECT count(*) INTO broj FROM pg_tables WHERE schemaname = 'public';
    IF broj <> 11 THEN
        RAISE EXCEPTION 'V017: očekivano 11 radnih tablica, nađeno %', broj;
    END IF;

    SELECT count(*) INTO broj FROM jelo;
    IF broj <> 317 THEN
        RAISE EXCEPTION 'V017: tablica jelo mora ostati netaknuta (317), nađeno %', broj;
    END IF;
END $$;
