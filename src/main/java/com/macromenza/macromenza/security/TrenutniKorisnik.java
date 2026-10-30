package com.macromenza.macromenza.security;

import com.macromenza.macromenza.model.Korisnik;
import com.macromenza.macromenza.repository.KorisnikRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Korisnik iz Supabase tokena (`sub` je njegov supabase_uid); kontroleri njime provjeravaju vlasništvo nad podacima. */
@Component
public class TrenutniKorisnik {

    private final KorisnikRepository korisnikRepository;

    public TrenutniKorisnik(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    public UUID supabaseUid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new PristupZabranjenException("Prijava je obavezna.");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new PristupZabranjenException("Token nema ispravnog korisnika.");
        }
    }

    /** Id prijavljenog korisnika u bazi; prije registracije profila korisnik još ne postoji. */
    public Long id() {
        return korisnikRepository.findBySupabaseUid(supabaseUid())
                .map(Korisnik::getId)
                .orElseThrow(() -> new PristupZabranjenException("Korisnik nije registriran."));
    }

    /** Baca 403 ako [idKorisnik] nije prijavljeni korisnik. */
    public void provjeri(Long idKorisnik) {
        if (idKorisnik == null) {
            throw new IllegalArgumentException("idKorisnik je obavezan.");
        }
        if (!idKorisnik.equals(id())) {
            throw new PristupZabranjenException("Nemaš pristup podacima drugog korisnika.");
        }
    }
}
