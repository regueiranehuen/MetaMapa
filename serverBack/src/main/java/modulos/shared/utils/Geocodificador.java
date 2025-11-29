package modulos.shared.utils;

import modulos.agregacion.entities.DbMain.Pais;
import modulos.agregacion.entities.DbMain.PaisProvincias;
import modulos.agregacion.entities.DbMain.Provincia;
import modulos.agregacion.entities.DbMain.UbicacionString;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;

import org.json.JSONObject;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Locale;

public final class Geocodificador {

    private static final WebClient webClient = WebClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "metamapa/1.0 (contacto: contacto@tudominio.com)")
            .build();

    private Geocodificador() {}

    public static UbicacionString obtenerUbicacion(Double lat, Double lon) {
        try {
            String url = String.format(Locale.US,
                    "/reverse?format=jsonv2&lat=%f&lon=%f&addressdetails=1&zoom=10&accept-language=es",
                    lat, lon);

            // Petición HTTP reactiva
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.empty();
                    })
                    .block(); // Bloquea solo hasta recibir respuesta (puede quitarse en contexto reactivo)

            if (response == null) return null;

            JSONObject root = new JSONObject(response);
            JSONObject address = root.optJSONObject("address");
            if (address == null) return null;

            String country = address.optString("country", null);
            String province = firstNonBlank(
                    address.optString("state", null),
                    address.optString("region", null),
                    address.optString("province", null),
                    address.optString("state_district", null),
                    address.optString("county", null)
            );

            if (country == null) return null;

            UbicacionString out = new UbicacionString();
            out.setPais(country);
            out.setProvincia(province != null ? province : "");

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<PaisProvincias> obtenerTodosLosPaises() {
        try {
            // 1) Países (RestCountries: nombre + ISO2)
            List<PaisIso2> paises = fetchPaisesRestCountries();

            // 2) Subdivisiones (Overpass: todas las relaciones con ISO3166-2)
            Map<String, List<String>> provinciasPorIso2 = fetchSubdivisionesISO31662Overpass();

            // 3) Armar resultado
            List<PaisProvincias> out = new ArrayList<>(paises.size());
            Collator coll = Collator.getInstance(new Locale("es"));

            for (PaisIso2 row : paises) {
                Pais pais = new Pais();
                pais.setPais(row.nombre);

                List<String> nombresProv = provinciasPorIso2.getOrDefault(row.iso2, Collections.emptyList());

                // construir entidades Provincia (deduplicadas, ordenadas)
                List<Provincia> provincias = new ArrayList<>(nombresProv.size());
                Set<String> vistos = new HashSet<>();
                for (String n : nombresProv) {
                    String key = n.trim().toLowerCase(Locale.ROOT);
                    if (vistos.add(key)) {
                        Provincia pr = new Provincia();
                        pr.setPais(pais);
                        pr.setProvincia(n);
                        provincias.add(pr);
                    }
                }
                provincias.sort(Comparator.comparing(Provincia::getProvincia, coll));

                // Si tenés constructor (Pais, List<Provincia>):
                out.add(new PaisProvincias(pais, provincias));

                // Si NO lo tenés, usá esta variante:
                /*
                PaisProvincias pp = new PaisProvincias();
                pp.setPais(pais);
                pp.setProvincias(provincias);
                out.add(pp);
                */
            }

            // Orden final por nombre de país
            out.sort(Comparator.comparing(
                    pp -> pp.getPais() != null ? pp.getPais().getPais() : "",
                    Collator.getInstance(new Locale("es"))
            ));

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ============================
    // 1) RESTCOUNTRIES (PAÍSES)
    // ============================
    private static List<PaisIso2> fetchPaisesRestCountries() throws IOException {
        String url = "https://restcountries.com/v3.1/all?fields=name,cca2";
        String json = httpGet(url, "metamapa/1.0 (contacto: tu-email@dominio)");

        JSONArray arr = new JSONArray(json);
        List<PaisIso2> out = new ArrayList<>(arr.length());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (!o.has("name")) continue;
            JSONObject name = o.getJSONObject("name");
            String common = name.optString("common", null);
            String iso2 = o.optString("cca2", null);
            if (common == null || iso2 == null || iso2.isBlank()) continue;

            out.add(new PaisIso2(common.trim(), iso2.trim().toUpperCase(Locale.ROOT)));
        }

        // Orden por nombre
        out.sort(Comparator.comparing(p -> p.nombre, Collator.getInstance(new Locale("es"))));
        return out;
    }

    // ==================================================
    // 2) OVERPASS (TODAS LAS SUBDIVISIONES ISO3166-2)
    // ==================================================
    /**
     * Trae en UNA llamada todas las rels con tag ISO3166-2 y agrupa por ISO2 (prefijo antes del guión).
     * Ej.: "AR-B" => clave "AR" con valor "Buenos Aires"
     */
    private static Map<String, List<String>> fetchSubdivisionesISO31662Overpass() throws IOException {
        // Query: todas las relaciones administrativas que tienen ISO3166-2
        String q = """
            [out:json][timeout:300];
            rel["boundary"="administrative"]["ISO3166-2"];
            out tags;
            """;

        String overpassUrl = "https://overpass-api.de/api/interpreter";
        String body = "data=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        String json = httpPost(overpassUrl, body, "metamapa/1.0 (contacto: tu-email@dominio)",
                "application/x-www-form-urlencoded; charset=UTF-8");

        JSONObject root = new JSONObject(json);
        JSONArray elements = root.optJSONArray("elements");
        Map<String, List<String>> porIso2 = new HashMap<>();
        if (elements == null) return porIso2;

        for (int i = 0; i < elements.length(); i++) {
            JSONObject e = elements.getJSONObject(i);
            JSONObject tags = e.optJSONObject("tags");
            if (tags == null) continue;

            String codigo = tags.optString("ISO3166-2", null); // ej. AR-B, US-CA
            if (codigo == null || !codigo.contains("-")) continue;

            String nombre = firstNonBlank(
                    tags.optString("name", null),
                    tags.optString("official_name", null),
                    tags.optString("short_name", null)
            );
            if (nombre == null || nombre.isBlank()) continue;

            String iso2 = codigo.substring(0, codigo.indexOf('-')).trim().toUpperCase(Locale.ROOT);
            porIso2.computeIfAbsent(iso2, k -> new ArrayList<>()).add(nombre.trim());
        }

        // Ordenar listas por nombre
        Collator coll = Collator.getInstance(new Locale("es"));
        for (List<String> lista : porIso2.values()) {
            lista.sort(coll);
        }
        return porIso2;
    }

    // ============================
    // HTTP helpers
    // ============================
    private static String httpGet(String url, String userAgent) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", userAgent);
        c.setRequestProperty("Accept", "application/json");
        c.setConnectTimeout(15_000);
        c.setReadTimeout(60_000);

        int status = c.getResponseCode();
        InputStream is = status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally { c.disconnect(); }
    }

    private static String httpPost(String url, String body, String userAgent, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("User-Agent", userAgent);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", contentType);
        c.setConnectTimeout(15_000);
        c.setReadTimeout(60_000);
        c.setDoOutput(true);

        try (OutputStream os = c.getOutputStream()) { os.write(bytes); }

        int status = c.getResponseCode();
        InputStream is = status >= 200 && status < 300 ? c.getInputStream() : c.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally { c.disconnect(); }
    }

    // ============================
    // Utils
    // ============================
    private static String firstNonBlank(String... vs) {
        for (String v : vs) if (v != null && !v.isBlank()) return v;
        return null;
    }

    /** Par (nombre país, ISO2) para armar después Pais + Provincias. */
    private record PaisIso2(String nombre, String iso2) {}
}
