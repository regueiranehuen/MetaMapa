package com.tpagrupo17.Metamapa.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FechaParser {

    // Se recomienda usar Locale.ROOT para formatos numéricos y estandarizados (ej. ISO)
    // Se elimina la redundancia. El formato 'yyyy-MM-dd'T'HH:mm' está incluido.
    private static final List<DateTimeFormatter> FORMATOS_LOCAL = Arrays.asList(
            // Formatos de fecha y hora (con 'T' o espacio)
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ROOT),
            // ESTE ES EL FORMATO QUE NECESITABAS (sin segundos)
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm", Locale.ROOT),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME, // yyyy-MM-ddTHH:mm:ss

            // Formatos solo de fecha (para el fallback a LocalDate)
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/M/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d-M-yyyy", Locale.ROOT)
    );

    private static final List<DateTimeFormatter> FORMATOS_ZONED = Arrays.asList(
            DateTimeFormatter.ISO_ZONED_DATE_TIME,    // 2023-10-05T10:15:30+01:00[Europe/Paris]
            DateTimeFormatter.ISO_OFFSET_DATE_TIME    // 2023-10-05T10:15:30+01:00
    );

    /**
     * Parsea una cadena de texto a LocalDateTime, probando ZonedDateTime y luego formatos locales.
     */
    public static LocalDateTime parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }

        fechaStr = fechaStr.trim();

        // 1. Intentar con formatos ZonedDateTime (siempre primero, ya que son más específicos)
        for (DateTimeFormatter formatter : FORMATOS_ZONED) {
            try {
                // Usamos ZonedDateTime.parse. Si tiene zona, lo convertimos a Local sin zona.
                ZonedDateTime zdt = ZonedDateTime.parse(fechaStr, formatter);
                return zdt.toLocalDateTime();
            } catch (DateTimeParseException ignored) {
            }
        }

        // 2. Intentar con formatos locales (LocalDateTime y luego LocalDate fallback)
        for (DateTimeFormatter formatter : FORMATOS_LOCAL) {
            // Intentar como LocalDateTime (fecha y hora)
            try {
                return LocalDateTime.parse(fechaStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Ignorar y probar siguiente...
            }

            // Fallback: Intentar como LocalDate (solo fecha) y establecer la hora a medianoche
            try {
                // Nota: Usamos el MISMO formatter que falló en LocalDateTime.parse()
                // Esto es crucial para que los formatos sin hora (como '2023-10-05') funcionen
                LocalDate date = LocalDate.parse(fechaStr, formatter);
                return date.atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    public static boolean sonMismaFecha(LocalDateTime fecha1, LocalDateTime fecha2) {
        if (fecha1 == null || fecha2 == null) return false;
        return fecha1.getDayOfMonth() == fecha2.getDayOfMonth()
                && fecha1.getMonthValue() == fecha2.getMonthValue()
                && fecha1.getYear() == fecha2.getYear();
    }


    public static void main(String[] args) {
        String[] fechasDePrueba = {
                "2000-01-01T00:00", // EL CASO QUE NO FUNCIONABA
                "2023-10-05T10:15:30+01:00[Europe/Paris]",
                "2023-10-05T10:15:30+01:00",
                "2023-12-01 14:30:00",
                "1/5/2017",
                "01/05/2017",
                "5/1/2017", // Este puede ser ambiguo según la cultura, pero se interpreta como d/M/yyyy
                "05/01/2017",
                "2023-10-05",
                "2023-10-05T10:15:30" // ISO con segundos
        };

        // Corrección: LocalDateTime no tiene zona horaria, así que quitamos 'z' del formatter de salida.
        DateTimeFormatter formatterSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ROOT);

        
        for (String fecha : fechasDePrueba) {
            try {
                LocalDateTime ldt = parsearFecha(fecha);
                String parsedOutput = (ldt != null) ? ldt.format(formatterSalida) : "null";
                
            } catch (Exception e) {
                // Manejo de error general para evitar que una fecha errónea detenga la prueba
                
            }
        }
        
    }
}