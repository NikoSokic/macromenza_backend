# MacroMenza – plan razvoja

Jedan korak = jedna funkcija, kompletno backend + frontend. Status: ⬜ nije započeto, ✅ gotovo.

| # | Korak | Status |
|---|---|---|
| 1 | Lokalno pokretanje (ispravak `postgres}` u URL-u, `BASE_URL` iz `local.properties`), pregled baze u `docs/STANJE_BAZE.md` | ✅ |
| 2 | Tablica `namirnica` (iz `namirnice.csv`) | ✅ |
| 3 | `jelo_sastojak`, uvoz 311 jela, SQL view s izračunom, decimalne vrijednosti, zamjena starih jela | ✅ |
| 4 | Meniji: `meni`, `meni_stavka` (84) + endpoint | ✅ |
| 5 | Obrok kao cjelina (`obrok`: datum, vrijeme, tip, izvor), spremanje u jednom pozivu, odabir datuma i vremena, porcije po 0,5 | ✅ |
| 6 | Pretraga po kategorijama i "Često jedeš" (filter po makronutrijentima ostaje) | ✅ |
| 7 | Detalj jela: sastojci, gramaža, napomena | ✅ |
| 8 | Meniji u unosu obroka | ✅ |
| 9 | Sažetak obroka pri dnu ekrana | ✅ |
| 10 | Uređivanje obroka | ✅ |
| 11 | Brisanje obroka s potvrdom | ✅ |
| 12 | Početni ekran: prsten kalorija po obrocima i savjet dana | ⬜ |
| 13 | Povijest: listanje tjedana, graf s linijom cilja, dani u cilju (±10 %), prosjek makronutrijenata | ⬜ |
| 14 | Masa s decimalom, brzo mjerenje ±0,1 kg, graf mase | ⬜ |
| 15 | Objašnjenje izračuna u onboardingu, ekran "Odakle su kalorije", brisanje svih podataka, primjer tjedna | ⬜ |
| 16 | Backend: prepoznavanje obroka sa slike ili opisa (Gemini API, ključ u env varijabli; prije implementacije provjeri aktualni model i besplatne limite) | ⬜ |
| 17 | Android: ekran "Slika ili opis" + dijeljenje screenshota u aplikaciju | ⬜ |
| 18 | Nova lozinka baze, vjerodajnice u env varijable, `.gitignore` | ⬜ |
| 19 | Supabase JWT autorizacija (korisnik iz tokena, zatvoriti `/korisnici`, ukloniti `lozinka_hash`) | ⬜ |
| 20 | Ispravni HTTP statusi grešaka + odluka o hostingu | ⬜ |

## Lokalno pokretanje (korak 1)

- Backend: `cd backend; .\mvnw.cmd spring-boot:run` (port 8080).
- Android: `BASE_URL` se čita iz `frontend/local.properties`, ključ `macromenza.baseUrl`; bez ključa je `http://10.0.2.2:8080/` (emulator → laptop). Nakon promjene vrijednosti treba ponovno buildati aplikaciju.
