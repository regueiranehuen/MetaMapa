package modulos.agregacion.controllers;

import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import modulos.JwtClaimExtractor;
import modulos.agregacion.services.SolicitudHechoService;
import modulos.shared.dtos.input.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/solicitud-hecho")
public class SolicitudHechoController {

    private final SolicitudHechoService solicitudHechoService;

    public SolicitudHechoController(SolicitudHechoService solicitudHechoService){
        this.solicitudHechoService = solicitudHechoService;
    }

    @GetMapping("/public/porcentajeSolicitudes")
    public ResponseEntity<?> getPorcentajeSolicitudes(){
        return solicitudHechoService.getPorcentajeSolicitudesProcesadas();
    }

    // Anda
    @PostMapping("/evaluar/subir")
    public ResponseEntity<?> evaluarSolicitudSubida(@Valid @RequestBody SolicitudHechoEvaluarInputDTO dtoInput, @AuthenticationPrincipal String username){
        return solicitudHechoService.evaluarSolicitudSubirHecho(dtoInput, username); // 200 o 401
    }

    // Anda
    @PostMapping("/evaluar/eliminar")
    public ResponseEntity<?> evaluarSolicitudEliminacion(@Valid @RequestBody SolicitudHechoEvaluarInputDTO dtoInput, @AuthenticationPrincipal String username){
        return solicitudHechoService.evaluarEliminacionHecho(dtoInput, username); // 200, 401
    }


    @PostMapping("/evaluar/modificar")
    public ResponseEntity<?> evaluarSolicitudModificacion(@Valid @RequestBody SolicitudHechoEvaluarInputDTO dtoInput, @AuthenticationPrincipal String username){
        return solicitudHechoService.evaluarModificacionHecho(dtoInput, username); // 200, 401
    }

    // Anda
    @PostMapping("/public/subir-hecho")
    public ResponseEntity<?> enviarSolicitudSubirHecho(@RequestPart("meta") SolicitudHechoInputDTO dto,
                                                       @RequestPart(value = "contenidosMultimedia", required = false) List<MultipartFile> files,
                                                       @AuthenticationPrincipal String username){
        return solicitudHechoService.solicitarSubirHecho(dto, files, username); // 200 o 401
    }

    // Anda
    @PostMapping("/eliminar-hecho")
    public ResponseEntity<?> enviarSolicitudEliminarHecho(@Valid @RequestBody SolicitudHechoEliminarInputDTO dtoInput, @AuthenticationPrincipal String username){
        return solicitudHechoService.solicitarEliminacionHecho(dtoInput, username); // 200 o 401
    }

    // Anda
    @PostMapping("/modificar-hecho")
    public ResponseEntity<?> enviarSolicitudModificarHecho(@Valid @RequestBody SolicitudHechoModificarInputDTO dtoInput, @AuthenticationPrincipal String username){
        return solicitudHechoService.solicitarModificacionHecho(dtoInput, username); // 200, 401 o 409 (recurso ya modificado)
    }

    // Anda
    @PostMapping("/reportes/reportar")
    public ResponseEntity<?> reportar(@Valid @RequestParam Long id_hecho, @Valid @RequestParam String fuente, @Valid @RequestParam String motivo){
        return solicitudHechoService.reportarHecho(motivo, id_hecho, fuente);
    }

    @GetMapping("/reportes/get/all")
    public ResponseEntity<?> getAllReportes(@AuthenticationPrincipal String username){
        return solicitudHechoService.getAllReportes(username);
    }

    @GetMapping("/reportes/evaluar")
    public ResponseEntity<?> evaluarReporte(@Valid @RequestBody EvaluarReporteInputDTO dtoInput, @AuthenticationPrincipal Jwt principal){
        return solicitudHechoService.evaluarReporte(dtoInput, principal);
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getAllSolicitudes(@AuthenticationPrincipal String username){
        return solicitudHechoService.getAllSolicitudes(username);
    }

    @GetMapping("/get/pendientes")
    public ResponseEntity<?> getSolicitudesPendientes(@AuthenticationPrincipal String username){
        return solicitudHechoService.obtenerSolicitudesPendientes(username);
    }

    @GetMapping("/atributos-hecho")
    public ResponseEntity<?> getAtributosSolicitudHecho(@Valid @RequestParam Long id_solicitud, @AuthenticationPrincipal String username){
        System.out.println("WAZAAA ME ENCANTA OBTENER ATRIBUTOS DEL HECHO A MODIFICAR");
        return solicitudHechoService.getAtributosSolicitudHecho(id_solicitud, username);
    }

}

