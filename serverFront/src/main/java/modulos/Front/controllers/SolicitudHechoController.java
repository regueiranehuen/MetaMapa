package modulos.Front.controllers;

import lombok.RequiredArgsConstructor;
import modulos.Front.services.SolicitudHechoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/solicitudes-hecho")
@RequiredArgsConstructor
public class SolicitudHechoController {
    private final SolicitudHechoService solicitudHechoService;


}