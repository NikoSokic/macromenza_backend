package com.macromenza.macromenza.it;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Ugovor backend ↔ Android: čita Kotlin data klase iz frontend/.../model i provjerava da JSON odgovor ima svako
 * polje (naziv iz @SerializedName ili naziv svojstva), da ne-nullable polja nisu null i da se tipovi slažu.
 */
public final class KotlinUgovor {

    static final Path MODEL = Path.of("..", "frontend", "app", "src", "main", "java",
            "com", "niko", "macromenza", "model");

    private static final Pattern KLASA = Pattern.compile("data class (\\w+)\\s*\\(");
    private static final Pattern SVOJSTVO = Pattern.compile(
            "(?:@SerializedName\\(\"([^\"]+)\"\\)\\s*)?(?:val|var)\\s+(\\w+)\\s*:\\s*([^=]+?)\\s*(?:=.*)?$",
            Pattern.DOTALL);

    record Polje(String json, String tip) {
        boolean nullable() {
            return tip.endsWith("?");
        }

        String bezUpitnika() {
            return nullable() ? tip.substring(0, tip.length() - 1).trim() : tip;
        }
    }

    private static Map<String, List<Polje>> klase;

    private KotlinUgovor() {
    }

    static boolean dostupan() {
        return Files.isDirectory(MODEL);
    }

    static synchronized Map<String, List<Polje>> klase() {
        if (klase == null) {
            klase = ucitaj();
        }
        return klase;
    }

    /** Provjerava [json] (Map/List iz JsonPath-a) prema Kotlin tipu, npr. "Obrok" ili "List<Jelo>". */
    public static void provjeri(Object json, String kotlinTip) {
        List<String> greske = new ArrayList<>();
        provjeriVrijednost(json, kotlinTip, kotlinTip, greske);
        if (!greske.isEmpty()) {
            fail("Odgovor nije usklađen s Kotlin modelom " + kotlinTip + ":\n  " + String.join("\n  ", greske));
        }
    }

    /** JSON nazivi svih polja Kotlin klase (za zahtjeve koje šalje aplikacija). */
    public static List<String> polja(String kotlinKlasa) {
        List<Polje> p = klase().get(kotlinKlasa);
        assertThat(p).as("Kotlin klasa " + kotlinKlasa).isNotNull();
        return p.stream().map(Polje::json).toList();
    }

    private static void provjeriVrijednost(Object v, String tip, String put, List<String> greske) {
        String t = tip.trim();
        boolean nullable = t.endsWith("?");
        if (nullable) {
            t = t.substring(0, t.length() - 1).trim();
        }

        if (v == null) {
            if (!nullable) {
                greske.add(put + ": null, a Kotlin tip " + tip + " nije nullable");
            }
            return;
        }

        if (t.startsWith("List<")) {
            if (!(v instanceof List<?> lista)) {
                greske.add(put + ": očekivana lista, dobiveno " + v.getClass().getSimpleName());
                return;
            }
            String unutra = t.substring(5, t.length() - 1);
            for (int i = 0; i < lista.size(); i++) {
                provjeriVrijednost(lista.get(i), unutra, put + "[" + i + "]", greske);
            }
            return;
        }

        if (t.startsWith("Map<")) {
            if (!(v instanceof Map<?, ?> mapa)) {
                greske.add(put + ": očekivan objekt (Map), dobiveno " + v.getClass().getSimpleName());
                return;
            }
            String vrijednost = t.substring(t.indexOf(',') + 1, t.length() - 1);
            mapa.forEach((k, x) -> provjeriVrijednost(x, vrijednost, put + "." + k, greske));
            return;
        }

        switch (t) {
            case "String" -> {
                if (!(v instanceof String)) greske.add(put + ": očekivan String, dobiveno " + v);
            }
            case "Long", "Int" -> {
                if (!(v instanceof Number n) || n.doubleValue() != Math.rint(n.doubleValue())) {
                    greske.add(put + ": očekivan cijeli broj, dobiveno " + v);
                }
            }
            case "Double", "Float" -> {
                if (!(v instanceof Number)) greske.add(put + ": očekivan broj, dobiveno " + v);
            }
            case "Boolean" -> {
                if (!(v instanceof Boolean)) greske.add(put + ": očekivan Boolean, dobiveno " + v);
            }
            default -> {
                List<Polje> polja = klase().get(t);
                if (polja == null) {
                    greske.add(put + ": nepoznat Kotlin tip " + t);
                    return;
                }
                if (!(v instanceof Map<?, ?> objekt)) {
                    greske.add(put + ": očekivan objekt " + t + ", dobiveno " + v);
                    return;
                }
                for (Polje p : polja) {
                    if (!objekt.containsKey(p.json())) {
                        greske.add(put + "." + p.json() + ": nema ga u odgovoru (Kotlin " + t + ")");
                        continue;
                    }
                    provjeriVrijednost(objekt.get(p.json()), p.tip(), put + "." + p.json(), greske);
                }
            }
        }
    }

    private static Map<String, List<Polje>> ucitaj() {
        Map<String, List<Polje>> rezultat = new HashMap<>();
        try (Stream<Path> datoteke = Files.list(MODEL)) {
            for (Path d : datoteke.filter(p -> p.toString().endsWith(".kt")).toList()) {
                String izvor = bezKomentara(Files.readString(d, StandardCharsets.UTF_8));
                Matcher m = KLASA.matcher(izvor);
                while (m.find()) {
                    int kraj = zatvorenaZagrada(izvor, m.end() - 1);
                    rezultat.put(m.group(1), parametri(izvor.substring(m.end(), kraj)));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return rezultat;
    }

    private static List<Polje> parametri(String tijelo) {
        List<Polje> polja = new ArrayList<>();
        for (String param : podijeli(tijelo)) {
            if (param.isBlank()) continue;
            Matcher m = SVOJSTVO.matcher(param.trim());
            if (!m.find()) {
                throw new IllegalStateException("Ne mogu pročitati Kotlin parametar: " + param);
            }
            polja.add(new Polje(m.group(1) != null ? m.group(1) : m.group(2), m.group(3).trim()));
        }
        return polja;
    }

    /** Dijeli po zarezima izvan (), <> i navodnika. */
    private static List<String> podijeli(String s) {
        List<String> dijelovi = new ArrayList<>();
        int dubina = 0;
        boolean string = false;
        int pocetak = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') string = !string;
            if (string) continue;
            if (c == '(' || c == '<') dubina++;
            if (c == ')' || c == '>') dubina--;
            if (c == ',' && dubina == 0) {
                dijelovi.add(s.substring(pocetak, i));
                pocetak = i + 1;
            }
        }
        dijelovi.add(s.substring(pocetak));
        return dijelovi;
    }

    private static int zatvorenaZagrada(String s, int otvorena) {
        int dubina = 0;
        for (int i = otvorena; i < s.length(); i++) {
            if (s.charAt(i) == '(') dubina++;
            if (s.charAt(i) == ')' && --dubina == 0) return i;
        }
        throw new IllegalStateException("Nezatvorena zagrada");
    }

    private static String bezKomentara(String s) {
        return s.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("(?m)//.*$", "");
    }
}
