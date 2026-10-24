# MacroMenza – upute za Claude Code

## O projektu
Privatna Android aplikacija za brojanje kalorija i makronutrijenata iz obroka u studentskoj menzi. Izrađena je kao završni rad (FESB), a sada je razvijam za vlastitu upotrebu. Kalorije se računaju iz službenih receptura menze (NN 120/2013) i tablice nutritivnih vrijednosti namirnica.

Autor: Niko. **Komuniciraj na hrvatskom**, kratko i konkretno.

## Struktura
```
macromenza/                  (nije git repozitorij)
  CLAUDE.md
  backend/   git: github.com/NikoSokic/macromenza_backend   (master)
  frontend/  git: github.com/NikoSokic/MacroMenza_Frontend  (master)
```
- **Backend:** Spring Boot 4, Java 17, Maven (`mvnw`), Spring Data JPA, PostgreSQL na Supabaseu, springdoc-openapi. Lombok je u `pom.xml`, ali se u kodu ne koristi (getteri/setteri su ručno pisani); u `pom.xml` je i nekorišteni Kotlin plugin. Paket `com.macromenza.macromenza` (`controller`, `dto`, `model`, `repository`, `services`). `ddl-auto=none`, pa se shema mijenja isključivo SQL skriptama.
- **Frontend:** Kotlin, Jetpack Compose + Material3, MVVM (ViewModel + StateFlow), Retrofit + Gson, DataStore (`UserSessionManager`, `ThemeManager`), Navigation Compose, Supabase Auth (email/lozinka, REST pozivi preko Retrofita u `SupabaseAuthApi`, bez Supabase SDK-a). Paket `com.niko.macromenza`.
- **Podaci:** `backend/db/podaci/` **još ne postoji**. Tamo trebaju doći `namirnice.csv`, `jela.csv`, `jelo_sastojak.csv`, `meniji.csv`, `meni_stavke.csv` i README.

## Pokretanje
Na laptopu je Windows i PowerShell 5.1 (nema `&&`, a skripte se pozivaju s `.\`).
- Backend radi lokalno na laptopu: `cd backend; .\mvnw.cmd spring-boot:run`, port 8080 (`server.port=${PORT:8080}`).
- Android build: `cd frontend; .\gradlew.bat assembleDebug`.
- Cilj nakon koraka 1: aplikacija se spaja na laptop, emulator preko `http://10.0.2.2:8080/`, mobitel preko IP-a laptopa na istoj Wi-Fi mreži (`usesCleartextTraffic="true"` je već u manifestu). Render se više ne koristi, ali je `BASE_URL` u kodu zasad još na Renderu (vidi "Trenutno stanje").

## Pravila rada
1. **Jedan korak plana = jedna funkcija, kompletno backend + frontend.** Ne radi ništa izvan zadanog koraka i ne refaktoriraj usput.
2. Sve što aplikacija sada radi mora nastaviti raditi.
3. **DTO-ovi moraju biti usklađeni s obje strane** (nazivi polja, `@SerializedName`, tipovi). Kad mijenjaš API, mijenjaš i Retrofit model i ViewModel.
4. **Promjene baze** idu kao numerirane SQL skripte u `backend/db/migracije/` (`V002__namirnica.sql`...), koje mi pokažeš prije pokretanja. Nikad ne briši ni ne mijenjaj postojeće podatke bez backup tablice ili izričite potvrde.
5. **Nikad ne ispisuj, ne kopiraj i ne premještaj lozinku baze, ključeve ni tokene.** Ne diraj autentikaciju ni `application.properties` vjerodajnice. Sigurnost je planirana za korake 18–20.
6. Nakon svake promjene: backend se mora kompajlirati i pokrenuti, `gradlew assembleDebug` mora proći, a nove endpointe testiraš pozivom i pokažeš odgovor. Ako nešto ne možeš provjeriti, reci to izričito.
7. Imenovanje je na hrvatskom, kao u postojećem kodu (`jelo`, `konzumacija`, `obrok`, `namirnica`, `mjerenje`...).
8. Dizajn ostaje isti: zelena `MacroGreen 0xFF7CB342`, `MacroLightGreen 0xFFA4D65E`, tamna pozadina `0xFF101411`, Material3.
9. **Na kraju svakog zadatka:** kratka lista promjena po repozitoriju, popis ručnih provjera u aplikaciji i **commit poruka za svaki promijenjeni repozitorij posebno** (hrvatski, naslov do 60 znakova, prazan red, natuknice). **Ne commitaj sam.**
10. Nakon dovršenog koraka označi ga kao gotov u `backend/docs/PLAN.md` (i u tablici ispod).

## Domenska pravila
- Vremenska zona `Europe/Zagreb`. Tipovi obroka: `DORUCAK`, `RUCAK`, `UZINA`, `VECERA`.
- Nutritivne vrijednosti jela **računaju se u bazi** (SQL view): zbroj nad `jelo_sastojak` × `namirnica` (vrijednost na 100 g × grami / 100). Kod "ili" alternativa u recepturi uzima se prva opcija. Za artikle "(kom)" koristi se `grama_po_komadu`.
- Vrijednosti su **decimalne** (ne Integer). Količina porcija ide u koracima od **0,5**.
- Vrijednosti su okvirne (USDA/EU tablice) i aplikacija to mora negdje jasno reći.
- Preporuka dnevnog unosa: Mifflin-St Jeor BMR × aktivnost (sjedilačka 1,2 / lagana 1,375 / umjerena 1,55 / visoka 1,725), cilj (mršavljenje −500, održavanje 0, dobivanje mase +300), proteini 2 g/kg, masti 25 % kcal / 9, ugljikohidrati ostatak / 4.

## Plan (20 koraka)
| # | Korak | Status |
|---|---|---|
| 1 | Lokalno pokretanje (ispravak `postgres}` u URL-u, `BASE_URL` iz `local.properties`), pregled baze u `docs/STANJE_BAZE.md` | ⬜ |
| 2 | Tablica `namirnica` (iz `namirnice.csv`) | ⬜ |
| 3 | `jelo_sastojak`, uvoz 311 jela, SQL view s izračunom, decimalne vrijednosti, zamjena starih jela | ⬜ |
| 4 | Meniji: `meni`, `meni_stavka` (84) + endpoint | ⬜ |
| 5 | Obrok kao cjelina (`obrok`: datum, vrijeme, tip, izvor), spremanje u jednom pozivu, odabir datuma i vremena, porcije po 0,5 | ⬜ |
| 6 | Pretraga po kategorijama i "Često jedeš" (filter po makronutrijentima ostaje) | ⬜ |
| 7 | Detalj jela: sastojci, gramaža, napomena | ⬜ |
| 8 | Meniji u unosu obroka | ⬜ |
| 9 | Sažetak obroka pri dnu ekrana | ⬜ |
| 10 | Uređivanje obroka | ⬜ |
| 11 | Brisanje obroka s potvrdom | ⬜ |
| 12 | Početni ekran: prsten kalorija po obrocima i savjet dana | ⬜ |
| 13 | Povijest: listanje tjedana, graf s linijom cilja, dani u cilju (±10 %), prosjek makronutrijenata | ⬜ |
| 14 | Masa s decimalom, brzo mjerenje ±0,1 kg, graf mase | ⬜ |
| 15 | Objašnjenje izračuna u onboardingu, ekran "Odakle su kalorije", brisanje svih podataka, primjer tjedna | ⬜ |
| 16 | Backend: prepoznavanje obroka sa slike ili opisa (Gemini API, ključ u env varijabli; prije implementacije provjeri aktualni model i besplatne limite) | ⬜ |
| 17 | Android: ekran "Slika ili opis" + dijeljenje screenshota u aplikaciju | ⬜ |
| 18 | Nova lozinka baze, vjerodajnice u env varijable, `.gitignore` | ⬜ |
| 19 | Supabase JWT autorizacija (korisnik iz tokena, zatvoriti `/korisnici`, ukloniti `lozinka_hash`) | ⬜ |
| 20 | Ispravni HTTP statusi grešaka + odluka o hostingu | ⬜ |

## Trenutno stanje (poznati problemi)
- `application.properties`: datasource URL završava s `}` (korak 1). Lozinka je u kodu i javno na GitHubu (korak 18).
- `Jelo` ima Integer vrijednosti; `Konzumacija.kolicina` je Integer.
- `KonzumacijaService.dodajKonzumaciju` uvijek postavlja današnji datum.
- Unos obroka sprema jelo po jelo, bez provjere uspjeha (korak 5).
- Totali se računaju s N+1 upitima (`jeloRepository.findById` po redu).
- `RetrofitInstance.BASE_URL` je tvrdo upisan na Render.
