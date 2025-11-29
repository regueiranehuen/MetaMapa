package modulos.shared.utils;

import org.springframework.cglib.core.Local;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FechaParser {

    private static final List<DateTimeFormatter> FORMATOS_LOCAL = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ROOT),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd/M/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d/MM/yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("d-M-yyyy", Locale.ROOT)
    );

    public static boolean sonMismaFecha(LocalDateTime fecha1, LocalDateTime fecha2) {
        if (fecha1 == null || fecha2 == null) return false;
        return fecha1.getDayOfMonth() == fecha2.getDayOfMonth()
                && fecha1.getMonthValue() == fecha2.getMonthValue()
                && fecha1.getYear() == fecha2.getYear();
    }

    private static final List<DateTimeFormatter> FORMATOS_ZONED = Arrays.asList(
            DateTimeFormatter.ISO_ZONED_DATE_TIME,    // 2023-10-05T10:15:30+01:00[Europe/Paris]
            DateTimeFormatter.ISO_OFFSET_DATE_TIME    // 2023-10-05T10:15:30+01:00
    );

    public static LocalDateTime parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }

        fechaStr = fechaStr.trim();

        // Intentar con formatos que ya son ZonedDateTime
        for (DateTimeFormatter formatter : FORMATOS_ZONED) {
            try {
                ZonedDateTime zdt = ZonedDateTime.parse(fechaStr, formatter);
                return zdt.toLocalDateTime();
            } catch (DateTimeParseException ignored) {
            }
        }

        // Intentar con formatos locales con hora
        for (DateTimeFormatter formatter : FORMATOS_LOCAL) {
            try {
                return LocalDateTime.parse(fechaStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Ignorar y probar siguiente
            }

            // Fallback a LocalDate si no tiene hora
            try {
                LocalDate date = LocalDate.parse(fechaStr, formatter);
                return date.atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }


    public static void main(String[] args) {
        String[] fechasDePrueba = {
                "2023-10-05T10:15:30+01:00[Europe/Paris]",
                "2023-10-05T10:15:30+01:00",
                "2023-12-01 14:30:00",
                "1/5/2017",
                "01/05/2017",
                "5/1/2017",
                "05/01/2017",
                "2023-10-05",
        };
        DateTimeFormatter formatterSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z", Locale.ENGLISH);

        for (String fecha : fechasDePrueba) {
            try {
                LocalDateTime zdt = parsearFecha(fecha);
                System.out.println("Entrada: " + fecha + " -> Parsed: " + zdt.format(formatterSalida));
            } catch (IllegalArgumentException e) {
                System.out.println("Entrada: " + fecha + " -> Error: " + e.getMessage());
            }
        }
    }
}
