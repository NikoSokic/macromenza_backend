# MacroMenza - Backend

Backend aplikacije MacroMenza razvijen je koristeći Spring Boot. Zadužen je za obradu poslovne logike, upravljanje korisnicima, izračun prehrambenih preporuka te komunikaciju s PostgreSQL bazom podataka.

## Funkcionalnosti

- Upravljanje korisnicima i onboarding podacima
- Izračun preporučenog dnevnog unosa kalorija i makronutrijenata
- Evidencija konzumiranih obroka
- Dohvat dnevne povijesti i tjednog pregleda prehrambenih podataka
- REST API za komunikaciju s Android aplikacijom

## Arhitektura sustava

## Arhitektura sustava

Aplikacija je razvijena prema klijent-server arhitekturi i sastoji se od tri sloja:

1. **Android aplikacija** — klijentski sloj koji korisnicima prikazuje sučelje i šalje zahtjeve prema backendu
2. **Spring Boot** — središnji sloj koji obrađuje poslovnu logiku, validira podatke i komunicira s bazom
3. **Supabase PostgreSQL** — podatkovni sloj koji pohranjuje sve korisničke i prehrambene podatke

Autentikacija korisnika odvija se putem Supabase Authentication servisa, dok Spring Boot backend upravlja svim podacima vezanim uz aplikaciju.

## Autentikacija

Backend ne upravlja lozinkama korisnika. Za autentikaciju se koristi Supabase Authentication. Nakon uspješne prijave, Android aplikacija backendu šalje Supabase UID, pomoću kojeg backend dohvaća ili kreira korisnika u vlastitoj bazi podataka te ga povezuje s podacima aplikacije.

## Korištene tehnologije

### Backend
- Java
- Spring Boot
- Spring Data JPA
- Maven

### Baza podataka
- PostgreSQL
- Supabase

### Deploy
- Render

## Autor

**Niko Sokić**  
Fakultet elektrotehnike, strojarstva i brodogradnje (FESB)  
Sveučilište u Splitu
