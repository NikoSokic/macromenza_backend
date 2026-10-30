package com.macromenza.macromenza.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

/**
 * Svi zahtjevi osim /api/ping traže Supabase access token (Authorization: Bearer ...).
 * Potpis se provjerava javnim ključevima projekta (JWKS, ES256), pa backend ne treba nikakvu tajnu.
 */
@Configuration
@EnableWebSecurity
public class SigurnosnaKonfiguracija {

    @Bean
    SecurityFilterChain sigurnosniLanac(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/ping").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((req, res, e) ->
                                odgovori(res, HttpServletResponse.SC_UNAUTHORIZED, "Prijava je obavezna ili je istekla."))
                        .accessDeniedHandler((req, res, e) ->
                                odgovori(res, HttpServletResponse.SC_FORBIDDEN, "Nemaš pristup.")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                odgovori(res, HttpServletResponse.SC_UNAUTHORIZED, "Prijava je obavezna ili je istekla."))
                        .accessDeniedHandler((req, res, ex) ->
                                odgovori(res, HttpServletResponse.SC_FORBIDDEN, "Nemaš pristup.")));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${supabase.url}") String supabaseUrl) {
        String baza = supabaseUrl.replaceAll("/+$", "") + "/auth/v1";
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(baza + "/.well-known/jwks.json")
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();

        OAuth2TokenValidator<Jwt> izdavac = JwtValidators.createDefaultWithIssuer(baza);
        OAuth2TokenValidator<Jwt> publika = new JwtClaimValidator<Object>("aud", aud ->
                aud instanceof String s ? "authenticated".equals(s)
                        : aud instanceof Iterable<?> it && contains(it, "authenticated"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(izdavac, publika));
        return decoder;
    }

    private static boolean contains(Iterable<?> vrijednosti, String trazena) {
        for (Object v : vrijednosti) {
            if (trazena.equals(v)) return true;
        }
        return false;
    }

    /** Greške sigurnosnog sloja nastaju prije MVC-a, pa se {"poruka": ...} piše ručno (poruke su naše, bez navodnika). */
    private static void odgovori(HttpServletResponse res, int status, String poruka) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getOutputStream().write(("{\"poruka\":\"" + poruka + "\"}").getBytes(StandardCharsets.UTF_8));
    }
}
