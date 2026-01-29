package com.tpagrupo17.Metamapa.controllers;

import com.tpagrupo17.Metamapa.services.SinonimoService;
import com.tpagrupo17.Metamapa.dtos.input.SinonimoInputDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sinonimos")
public class SinonimoController {

    private final SinonimoService sinonimoService;

    public SinonimoController(SinonimoService sinonimoService) {
        this.sinonimoService = sinonimoService;
    }

    @PostMapping("/crear/categoria")
    public ResponseEntity<?> crearSinonimoCategoria(@RequestBody SinonimoInputDto sinonimoDTO, @AuthenticationPrincipal String username){
        return sinonimoService.crearSinonimoCategoria(username, sinonimoDTO);
    }

    @PostMapping("/crear/pais")
    public ResponseEntity<?> crearSinonimoPais(@RequestBody SinonimoInputDto sinonimoDTO, @AuthenticationPrincipal String username){
        return sinonimoService.crearSinonimoPais(username, sinonimoDTO);
    }

    @PostMapping("/crear/provincia")
    public ResponseEntity<?> crearSinonimoProvincia(@RequestBody SinonimoInputDto sinonimoDTO, @AuthenticationPrincipal String username){
        return sinonimoService.crearSinonimoProvincia(username, sinonimoDTO);
    }

}
