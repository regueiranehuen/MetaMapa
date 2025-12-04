package modulos.shared.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DetectorDeSpam {
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


    public static boolean esSpam(String texto) {

        if (texto == null){
            return false;
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


        if (esTextoRepetitivo(textoNormalizado)) {
            return true;
        }


        if (esRelleno(texto)) {
            return true;
        }

        if (mayusExcesivas(texto)) {
            return true;
        }

        return false;
    }

    private static boolean esTextoRepetitivo(String texto) {
        String[] palabras = texto.trim().split("\\s+");

        // Evitar falsos positivos en títulos/entradas cortas
        if (palabras.length < 5) return false;

        Map<String, Long> frecuencia = Arrays.stream(palabras)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        long maxRepeticiones = frecuencia.values().stream().max(Long::compare).orElse(0L);
        return (double) maxRepeticiones / palabras.length > 0.5;
    }


    private static boolean esRelleno(String texto) {
        // Detectar si el texto está lleno de caracteres de relleno
        long caracteresDeRelleno = texto.chars()
                .filter(c -> c == '.' || c == '-' || c == '_' || c == ' ')
                .count();

        return (double) caracteresDeRelleno / texto.length() > 0.3;
    }

    private static boolean mayusExcesivas(String texto) {
        long mayusculas = texto.chars().filter(Character::isUpperCase).count();
        long letras = texto.chars().filter(Character::isLetter).count();

        return letras > 0 && (mayusculas * 1.0 / letras) > 0.5;
    }
}
