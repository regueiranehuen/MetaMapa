package services.impl;

import services.IDetectorDeSpam;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DetectorDeSpam implements IDetectorDeSpam {

    // Lista de palabras/patrones comunes en spam
    private static final List<String> PALABRAS_SPAM = Arrays.asList(
            "oferta especial", "gratis", "promoción", "descuento",
            "click aquí", "urgente", "dinero fácil", "premio",
            "congratulations", "winner", "lottery", "prize"
    );

    // Patrones sospechosos
    private static final Pattern PATRON_REPETICION_EXCESIVA = Pattern.compile("(.)\\1{10,}"); // Para detectar 10 caracteres consecutivos
    private static final Pattern PATRON_MAYUSCULAS_EXCESIVAS = Pattern.compile("[A-Z]{50,}"); // Para que no ponga muchas letras en mayus
    private static final Pattern PATRON_NUMEROS_TELEFONO = Pattern.compile("\\b\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b"); // Para detectar numeros de telefono
    private static final Pattern PATRON_URLS_SOSPECHOSAS = Pattern.compile("http[s]?://[^\\s]+"); // Para detectar URL


    private List<String> corpusEvaluado = new ArrayList<>(); // Corpus dinámico

    @Override
    public boolean esSpam(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.length() < 500) {
            return true; // Texto vacío o corto no se puede recibir
        }

        String textoNormalizado = texto.toLowerCase().trim();

        // Verificar palabras spam
        for (String palabraSpam : PALABRAS_SPAM) {
            if (textoNormalizado.contains(palabraSpam.toLowerCase())) {
                return true;
            }
        }

        // Verificar patrones sospechosos
        if (PATRON_REPETICION_EXCESIVA.matcher(texto).find()) {
            return true; // Repetición excesiva de caracteres
        }

        if (PATRON_MAYUSCULAS_EXCESIVAS.matcher(texto).find()) {
            return true; // Demasiadas mayúsculas consecutivas
        }

        if (PATRON_NUMEROS_TELEFONO.matcher(texto).find()) {
            return true; // Demasiadas mayúsculas consecutivas
        }

        // Contar URLs - más de 2 URLs es sospechoso
        long cantidadUrls = PATRON_URLS_SOSPECHOSAS.matcher(texto).results().count();
        if (cantidadUrls > 2) {
            return true;
        }

        // Verificar si es solo texto repetitivo (muy básico)
        if (esTextoRepetitivo(textoNormalizado)) {
            return true;
        }

        // Si el texto es muy corto para la cantidad de caracteres requerida
        // y está lleno de caracteres de relleno
        if (texto.length() >= 500 && esRelleno(texto)) {
            return true;
        }

        /*
        // Corpus de referencia para calcular IDF
        List<String> corpus = Arrays.asList(
                "oferta especial descuento",
                "gratis promoción premio",
                "dinero fácil urgente"
        );
        Map<String, Double> idfMap = calcularIDF(corpus);

        // Calcular TF-IDF para el texto
        Map<String, Double> tfidfMap = calcularTFIDF(texto, idfMap);

        // Verificar si alguna palabra con alto TF-IDF está en la lista de palabras spam
        for (Map.Entry<String, Double> entry : tfidfMap.entrySet()) {
            String palabra = entry.getKey();
            double tfidf = entry.getValue();
            if (tfidf > 0.5 && PALABRAS_SPAM.contains(palabra)) { // Umbral ajustable
                return true;
            }
        }
*/
        //
        return false;
    }
/*
    private Map<String, Double> calcularIDF(List<String> corpus) {
        Map<String, Integer> docFrequency = new HashMap<>();
        int totalDocs = corpus.size();

        for (String documento : corpus) {
            Set<String> palabrasUnicas = Arrays.stream(documento.toLowerCase().split("\\s+"))
                    .collect(Collectors.toSet());
            for (String palabra : palabrasUnicas) {
                docFrequency.put(palabra, docFrequency.getOrDefault(palabra, 0) + 1);
            }
        }

        Map<String, Double> idfMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : docFrequency.entrySet()) {
            idfMap.put(entry.getKey(), Math.log((double) totalDocs / entry.getValue()));
        }

        return idfMap;
    }

    private Map<String, Double> calcularTFIDF(String texto, Map<String, Double> idfMap) {
        Map<String, Integer> tfMap = new HashMap<>();
        String[] palabras = texto.toLowerCase().split("\\s+");

        for (String palabra : palabras) {
            tfMap.put(palabra, tfMap.getOrDefault(palabra, 0) + 1);
        }

        Map<String, Double> tfidfMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tfMap.entrySet()) {
            String palabra = entry.getKey();
            double tf = (double) entry.getValue() / palabras.length;
            double idf = idfMap.getOrDefault(palabra, 0.0); // Usar idfMap correctamente
            tfidfMap.put(palabra, tf * idf);
        }

        return tfidfMap;
    }

    */

    private boolean esTextoRepetitivo(String texto) {
        String[] palabras = texto.split("\\s+");

        if (palabras.length < 30) return false;

        Map<String, Long> frecuencia = Arrays.stream(palabras)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.groupingBy(p -> p, Collectors.counting())); // para agrupar las palabras con sus cantidades

        long maxRepeticiones = frecuencia.values().stream().max(Long::compare).orElse(0L);
        return (double) maxRepeticiones / palabras.length > 0.6;

        // Saco el mayor numero de palabras repetidas
        // Si más del 60% de las palabras son la misma, el texto es considerado repetitivo
    }

    private boolean esRelleno(String texto) {
        // Detectar si el texto está lleno de caracteres de relleno
        long caracteresDeRelleno = texto.chars()
                .filter(c -> c == '.' || c == '-' || c == '_' || c == ' ')
                .count();

        return (double) caracteresDeRelleno / texto.length() > 0.3;
    }
}
