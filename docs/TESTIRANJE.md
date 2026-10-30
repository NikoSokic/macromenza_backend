# Testiranje MacroMenze

Tri razine automatskih testova i kratka lista ručnih provjera. Sve naredbe su za PowerShell 5.1 (bez `&&`).

| Razina | Gdje | Broj | Naredba |
|---|---|---|---|
| Backend: unit + integracijski (prava baza, rollback) | `backend/src/test` | 130 (1 preskočen) | `cd backend; .\mvnw.cmd test` |
| Frontend: unit, ugovor s backendom, pravila dizajna, snimke ekrana | `frontend/app/src/test` | 472 | `cd frontend; .\gradlew.bat verifyRoborazziDebug` |
| Frontend: E2E na emulatoru protiv lokalnog backenda | `frontend/app/src/androidTest` | 26 | vidi niže (trenutno ne prolazi, vidi „E2E na emulatoru“) |

Gradle traži JDK 17 (`$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"`).

## Backend

`.\mvnw.cmd test` pokreće unit testove servisa (Mockito) i integracijske testove svih kontrolera (`MockMvc`, prava Supabase baza).
Svaki integracijski test radi u transakciji koja se poništava, a `BezOstatakaIT` pada ako u bazi ostane testni korisnik
(e-mail `@macromenza.test`) i ispisuje broj redova glavnih tablica. Treba mreža do Supabasea; traje oko 2 minute.

Gemini se u testovima nikad ne zove (`@MockitoBean GeminiKlijent`).

### Prijava u testovima
Backend traži Supabase token na svakom zahtjevu osim `/api/ping`. Integracijski testovi ne zovu Supabase: Spring
Security test (`jwt()`) postavlja token sa zadanim `sub`. `IntegracijskiTest` svaki zahtjev šalje kao korisnik
`trenutniUid`. `noviKorisnik()` stvara korisnika i prebacuje se na njega, `kao(uid)` mijenja korisnika, a
`kao(null)` šalje zahtjev bez tokena.

`SigurnostIT` provjerava:
- bez tokena dobiva se 401, a `/api/ping` je otvoren;
- tuđi `idKorisnik` na svakom endpointu daje 403;
- uid u tijelu zahtjeva mora odgovarati tokenu;
- odgovor korisnika nema lozinku;
- uklonjeni endpointi više ne postoje;
- greške imaju ispravne statuse (400, 404, 405).

### Snimanje stvarnih odgovora za Android testove
`SnimiOdgovoreIT` je po zadanom isključen. Kad se promijeni oblik odgovora API-ja:

```powershell
cd backend
.\mvnw.cmd test -Dtest=SnimiOdgovoreIT "-Dsnimi.odgovore=../frontend/app/src/test/resources/odgovori"
```

## Frontend (bez emulatora)

`.\gradlew.bat testDebugUnitTest` pokreće ViewModele (lažni `ApiService`, virtualno vrijeme), logiku ekrana,
`ApiUgovorTest` (stvarni JSON backenda kroz Retrofit/Gson, putanje i nazivi polja) i `PravilaDizajnaTest`
(boje, dp/sp izvan `ui/theme`, gradijenti, fontovi, emoji, razmaci na rubu stringa).

### Vizualni testovi (Roborazzi)
Svaki `@Preview` (233) ima referentnu sliku u `app/src/test/snapshots`.

```powershell
.\gradlew.bat verifyRoborazziDebug   # usporedba s referencama (pada na svaku promjenu izgleda)
.\gradlew.bat recordRoborazziDebug   # nove reference nakon NAMJERNE promjene izgleda
```
Nakon `verify` razlike su u `app/build/outputs/roborazzi/*_compare.png` (staro | razlika | novo). Nove reference
pregledaj prije commita.

## E2E na emulatoru

> **Trenutno ne prolazi.** Od koraka 19 backend traži Supabase token, a E2E testovi korisnika stvaraju i sesiju
> upisuju bez prijave, pa dobivaju 401. Za popravak testovi trebaju pravi token, na primjer prijavom testnim
> Supabase računom (e-mail i lozinka u `local.properties`, izvan gita).

Aplikacija radi pravi tok (backend na laptopu, Supabase baza). Svaki test stvara testnog korisnika preko
`POST /korisnici/registracija-profil` i upisuje sesiju izravno u DataStore, pa se **Supabase prijava ne dodiruje**.
Nakon testa briše se sve što je korisnik napravio, profil i sam račun; ako išta ostane, test pada.

**Prije prvog pokretanja** (jednokratno) treba poseban AVD, jer tvoj `Pixel_3a` ima punu particiju:
kopiraj `~\.android\avd\Pixel_3a.avd\config.ini` u `MacroMenza_E2E.avd\config.ini` (promijeni `AvdId`, `displayname`,
`disk.dataPartition.size=4G`, `hw.sdCard=no`) i napravi `MacroMenza_E2E.ini` koji pokazuje na tu mapu.

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
# 1536 MB: s 2048 MB akcelerator (AEHD) na ovom računalu pada s "aehd_set_phys_mem"
Start-Process "$sdk\emulator\emulator.exe" -WindowStyle Hidden -ArgumentList '-avd','MacroMenza_E2E','-no-window','-no-audio','-no-snapshot','-gpu','swiftshader_indirect','-memory','1536'
& "$sdk\platform-tools\adb.exe" -s emulator-5554 reverse tcp:8080 tcp:8080     # localhost:8080 -> backend na laptopu
foreach ($k in 'window_animation_scale','transition_animation_scale','animator_duration_scale') { & "$sdk\platform-tools\adb.exe" -s emulator-5554 shell settings put global $k 0 }
```

Backend mora raditi (`cd backend; .\mvnw.cmd spring-boot:run`). Zatim:

```powershell
$env:ANDROID_SERIAL = "emulator-5554"   # OBAVEZNO: bez toga Gradle pokreće testove i na spojenom mobitelu
cd frontend; .\gradlew.bat connectedDebugAndroidTest
```

> **Pažnja:** `connectedDebugAndroidTest` bez `ANDROID_SERIAL` radi na SVIM spojenim uređajima i na kraju deinstalira
> aplikaciju, čime se na fizičkom mobitelu briše prijava. Uvijek postavi `ANDROID_SERIAL` na emulator.

Jedan razred: `-Pandroid.testInstrumentationRunnerArguments.class=com.niko.macromenza.e2e.ObrociE2ETest`.
Pri isteku čekanja test ispisuje što se vidi na ekranu (logcat oznaka `E2E`): `adb -s emulator-5554 logcat -d -s E2E:I`.

`SlikaIliOpisE2ETest` zove pravi Gemini jednom; bez ključa ili kvote test se preskače (nije greška).

### Što E2E pokriva
Početna (cilj iz backenda, navigacija), unos obroka (ručno, meni, tip obroka), uređivanje, brisanje i „Poništi“,
Povijest (tjedan, prošli tjedan, dan, primjer tjedna), Moje mjere (brzo mjerenje), Postavke ciljeva, tema, status
sustava, brisanje svih podataka (traži upis OBRIŠI), Odakle su kalorije, Slika ili opis, validacije prijave i registracije.

## Poznati nedostaci koje testovi bilježe
- `PovijestE2ETest.primjerTjednaSeUcitavaIUklanjaSamoZaPrazneDane` pada: `POST /api/obroci/primjer-tjedna` traje oko
  16 s (mnogo pojedinačnih upisa u udaljenu bazu), a Retrofit klijent aplikacije ima zadani timeout od 10 s. Aplikacija
  javi grešku veze iako backend podatke upiše.
- Obrazac „Postavke ciljeva“ se ne popunjava iz profila (visina, dob i masa uvijek kreću prazni).
- `CiljeviViewModel.spremiCilj` šalje datum po vremenu uređaja, ne po `Europe/Zagreb`.

## Ručne provjere (ne mogu se automatizirati)
- [ ] Pravi mobitel preko Wi-Fi IP-a laptopa (ili `adb reverse tcp:8080 tcp:8080`): cijeli tok unosa obroka.
- [ ] Registracija i prijava sa stvarnim Supabaseom, potvrda e-maila, onboarding novog korisnika, odjava.
- [ ] Istek tokena: nakon više od sat vremena aplikacija i dalje radi (token se sam osvježi). Ako refresh token više
      ne vrijedi, aplikacija vraća na prijavu.
- [ ] Render: prvi poziv nakon uspavanog servisa (do minute) ne javlja grešku veze.
- [ ] Kamera: „Fotografiraj“ vraća sliku u ekran (FileProvider se u Robolectricu ne može provjeriti), „Iz galerije“.
- [ ] Dijeljenje slike iz druge aplikacije (Galerija → Podijeli → MacroMenza) otvara „Slika ili opis“.
- [ ] Rotacija ekrana usred unosa obroka i uređivanja (stanje se ne gubi).
- [ ] Prekid mreže (avionski način) na Početnoj, Povijesti, Profilu i pri spremanju: poruke i „Pokušaj ponovno“.
- [ ] Tamna tema i sistemski font 1,3× na pravom uređaju; TalkBack: čitanje pladnja, grafa tjedna i koračnika porcija.
- [ ] Osjećaj animacija i povlačenja ulijevo za brisanje obroka na Početnoj (`Motion.kt`), odjava i povratak.
- [ ] Vizualni pregled snimki koje još nisu pregledane: Registracija, Postavke aplikacije/ciljeva, Uredi profil, Splash,
      Welcome, Odakle su kalorije, Dan, Sažetak, Dijalog, Konzumacija, Obrok spremljen.
