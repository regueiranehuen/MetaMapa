package com.tpagrupo17.Metamapa.servicioEstadistica.controller;

import com.tpagrupo17.Metamapa.servicioEstadistica.ServicioDeEstadistica;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {
    private ServicioDeEstadistica servicioDeEstadistica;

    public EstadisticasController(ServicioDeEstadistica servicioDeEstadistica) {
        this.servicioDeEstadistica = servicioDeEstadistica;
    }

    @GetMapping(value = "/reporte/estadisticaSpam.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarCSVestadisticaSpam() throws Exception {

        Path csv = servicioDeEstadistica.exportarEstadisticaSpam();

        if (csv == null){
            return ResponseEntity.noContent().build();
        }

        String filename = csv.getFileName().toString();

        StreamingResponseBody body = output -> {
            try (var in = java.nio.file.Files.newInputStream(csv)) {
                in.transferTo(output);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping(value = "/reporte/estadisticaCategoriaMasHechos.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarCSVestadisticaCategoriaMasHechos() throws Exception {

        Path csv = servicioDeEstadistica.exportarEstadisticaCategoriaMayorCantidadHechos();

        if (csv == null){
            return ResponseEntity.noContent().build();
        }

        String filename = csv.getFileName().toString();

        StreamingResponseBody body = output -> {
            try (var in = java.nio.file.Files.newInputStream(csv)) {
                in.transferTo(output);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping(value = "/reporte/estadisticaHoraMasHechosXCategoria.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarCSVestadisticaHoraMayorCantidadHechosCategorias() throws Exception {

        Path csv = servicioDeEstadistica.exportarEstadisticaHoraMayorCantidadHechosCategorias();

        if (csv == null){
            return ResponseEntity.noContent().build();
        }

        String filename = csv.getFileName().toString();

        StreamingResponseBody body = output -> {
            try (var in = java.nio.file.Files.newInputStream(csv)) {
                in.transferTo(output);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping(value = "/reporte/estadisticaProvinciaMasHechosXCategoria.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarCSVestadisticaMayorCantidadHechosCategoriasProvincias() throws Exception {

        Path csv = servicioDeEstadistica.exportarEstadisticaMayorCantidadHechosCategoriasProvincias();

        if (csv == null){
            return ResponseEntity.noContent().build();
        }

        String filename = csv.getFileName().toString();

        StreamingResponseBody body = output -> {
            try (var in = java.nio.file.Files.newInputStream(csv)) {
                in.transferTo(output);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @GetMapping(value = "/reporte/estadisticaProvinciaMayorCantidadHechosColecciones.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarCSVestadisticaProvinciaMayorCantidadHechosColecciones() throws Exception {

        Path csv = servicioDeEstadistica.exportarEstadisticaProvinciaMayorCantidadHechosColecciones();

        if (csv == null){
            return ResponseEntity.noContent().build();
        }

        String filename = csv.getFileName().toString();

        StreamingResponseBody body = output -> {
            try (var in = java.nio.file.Files.newInputStream(csv)) {
                in.transferTo(output);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

}
