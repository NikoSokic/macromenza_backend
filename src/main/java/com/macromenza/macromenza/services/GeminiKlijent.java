package com.macromenza.macromenza.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Base64;

/**
 * Poziv Gemini Interactions API-ja (POST /v1beta/interactions) preko RestClienta. Ključ ide samo u header
 * x-goog-api-key; ne logira se i ne ulazi ni u jednu poruku greške.
 */
@Component
public class GeminiKlijent {

    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/interactions";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GeminiKlijent.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiKlijent(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT);
        factory.setReadTimeout(TIMEOUT);

        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
    }

    public boolean imaKljuc() {
        return !apiKey.isEmpty();
    }

    public String getModel() {
        return model;
    }

    /** Vraća JSON tekst koji je model proizveo prema shemi (ili baca PrepoznavanjeException). */
    public String prepoznaj(String sistemskaUputa, String prompt, byte[] slika, String mimeSlike) {

        String zahtjev;
        try {
            zahtjev = mapper.writeValueAsString(gradiZahtjev(sistemskaUputa, prompt, slika, mimeSlike));
        } catch (Exception e) {
            throw new PrepoznavanjeException(HttpStatus.BAD_GATEWAY, "Prepoznavanje nije uspjelo (zahtjev).");
        }

        String odgovor;
        try {
            odgovor = restClient.post()
                    .uri(URL)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(zahtjev)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                throw new PrepoznavanjeException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Dnevni limit prepoznavanja je potrošen, pokušaj kasnije"
                );
            }
            // Tijelo odgovora se ne prosljeđuje klijentu; u log ide samo Googleova poruka greške (bez ključa).
            LOG.warn("Gemini je vratio {}: {}", e.getStatusCode().value(), porukaGreske(e));
            throw new PrepoznavanjeException(
                    HttpStatus.BAD_GATEWAY,
                    "Servis za prepoznavanje vratio je grešku (" + e.getStatusCode().value() + ")."
            );
        } catch (ResourceAccessException e) {
            throw new PrepoznavanjeException(
                    HttpStatus.BAD_GATEWAY,
                    "Servis za prepoznavanje nije odgovorio na vrijeme."
            );
        } catch (Exception e) {
            LOG.warn("Poziv Geminija nije uspio: {}", e.getClass().getSimpleName());
            throw new PrepoznavanjeException(HttpStatus.BAD_GATEWAY, "Prepoznavanje nije uspjelo.");
        }

        return izvuciTekst(odgovor);
    }

    private String porukaGreske(RestClientResponseException e) {
        try {
            String poruka = mapper.readTree(e.getResponseBodyAsString()).path("error").path("message").asText("");
            if (!apiKey.isEmpty()) poruka = poruka.replace(apiKey, "***");
            return poruka.length() > 300 ? poruka.substring(0, 300) : poruka;
        } catch (Exception ex) {
            return "(nečitljivo tijelo odgovora)";
        }
    }

    private ObjectNode gradiZahtjev(String sistemskaUputa, String prompt, byte[] slika, String mimeSlike) {
        ObjectNode korijen = mapper.createObjectNode();
        korijen.put("model", model);
        korijen.put("system_instruction", sistemskaUputa);
        korijen.put("store", false);

        ArrayNode input = korijen.putArray("input");
        input.addObject().put("type", "text").put("text", prompt);

        if (slika != null) {
            input.addObject()
                    .put("type", "image")
                    .put("data", Base64.getEncoder().encodeToString(slika))
                    .put("mime_type", mimeSlike);
        }

        ObjectNode format = korijen.putObject("response_format");
        format.put("type", "text");
        format.put("mime_type", "application/json");
        format.set("schema", shema());

        return korijen;
    }

    /** { stavke: [{ idJelo|null, nazivSlobodno, kolicina, sigurnost }], napomena } */
    private ObjectNode shema() {
        ObjectNode shema = mapper.createObjectNode();
        shema.put("type", "object");

        ObjectNode svojstva = shema.putObject("properties");

        ObjectNode stavke = svojstva.putObject("stavke");
        stavke.put("type", "array");

        ObjectNode stavka = stavke.putObject("items");
        stavka.put("type", "object");

        ObjectNode s = stavka.putObject("properties");
        s.putObject("idJelo").putArray("type").add("integer").add("null");
        s.putObject("nazivSlobodno").put("type", "string");
        s.putObject("kolicina").put("type", "number");
        ObjectNode sigurnost = s.putObject("sigurnost");
        sigurnost.put("type", "string");
        sigurnost.putArray("enum").add("VISOKA").add("SREDNJA").add("NISKA");

        stavka.putArray("required").add("idJelo").add("nazivSlobodno").add("kolicina").add("sigurnost");

        svojstva.putObject("napomena").put("type", "string");

        shema.putArray("required").add("stavke").add("napomena");
        return shema;
    }

    /** Tekst modela je u steps[].content[].text za korake tipa model_output. */
    private String izvuciTekst(String odgovor) {
        try {
            JsonNode korijen = mapper.readTree(odgovor);

            String status = korijen.path("status").asText("");
            if (!status.isEmpty() && !status.equals("completed")) {
                throw new PrepoznavanjeException(HttpStatus.BAD_GATEWAY, "Prepoznavanje nije dovršeno (" + status + ").");
            }

            StringBuilder tekst = new StringBuilder();
            for (JsonNode korak : korijen.path("steps")) {
                if (!"model_output".equals(korak.path("type").asText())) continue;
                for (JsonNode dio : korak.path("content")) {
                    if (dio.hasNonNull("text")) tekst.append(dio.get("text").asText());
                }
            }

            if (tekst.isEmpty()) {
                throw new PrepoznavanjeException(HttpStatus.BAD_GATEWAY, "Servis za prepoznavanje vratio je prazan odgovor.");
            }
            return tekst.toString();
        } catch (PrepoznavanjeException e) {
            throw e;
        } catch (Exception e) {
            throw new PrepoznavanjeException(HttpStatus.BAD_GATEWAY, "Odgovor servisa za prepoznavanje nije čitljiv.");
        }
    }
}
