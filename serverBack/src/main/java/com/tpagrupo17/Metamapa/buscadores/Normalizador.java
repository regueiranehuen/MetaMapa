package com.tpagrupo17.Metamapa.buscadores;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Normalizador {

    public static String normalizar(String texto) {
        if (texto == null) return "";
        // Quitar BOM y espacios invisibles (ZWSP, NBSP, etc.)
        String cleaned = texto
                .replace("\uFEFF", "")   // BOM
                .replace("\u200B", "")   // zero-width space
                .replace("\u200C", "")   // ZWNJ
                .replace("\u200D", "")   // ZWJ
                .replace("\u2060", "")   // word joiner
                .replace("\u00A0", " "); // NBSP -> espacio normal

        String sinTildes = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Quitar espacios normales y separadores Unicode
        return sinTildes.replaceAll("[\\p{Zs}\\s]+", "").toLowerCase(Locale.ROOT).trim();
    }


    public static List<String> normalizarSeparado(String texto) {
        return Arrays.stream(texto.toLowerCase().split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }


    public static Boolean normalizarYComparar(String s1, String s2){
        s1=normalizar(s1);
        s2=normalizar(s2);

        return s1.equals(s2);
    }
}
