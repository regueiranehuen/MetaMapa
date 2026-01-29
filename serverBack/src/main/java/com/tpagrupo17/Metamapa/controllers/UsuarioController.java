package com.tpagrupo17.Metamapa.controllers;

import com.tpagrupo17.Metamapa.dtos.input.*;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Rol;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Usuario;
import com.tpagrupo17.Metamapa.services.UsuarioService;
import com.tpagrupo17.Metamapa.dtos.output.AuthResponseDTO;
import com.tpagrupo17.Metamapa.JWT.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Anda
    @PostMapping("/public/crear")
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody UsuarioInputDTO dtoInput){
        
        return usuarioService.crearUsuario(dtoInput);
    }

    // Anda
    @PostMapping("/editar/contrasenia")
    public ResponseEntity<?> cambiarContrasenia(@RequestBody CambiarContraseniaDtoInput dtoImput, @AuthenticationPrincipal String username){
        return usuarioService.cambiarContrasenia(dtoImput, username);
    }

    // Anda
    @PostMapping("/editar/campos-escalares")
    public ResponseEntity<?> editarUsuario(@RequestBody EditarUsuarioDtoInput dtoImput, @AuthenticationPrincipal String username){
        return usuarioService.editarUsuario(dtoImput, username);
    }

    // Anda
    @PostMapping("/editar/nombre-usuario")
    public ResponseEntity<?> editarNombreDeUsuario(@RequestBody EditarNombreDeUsuarioDtoInput dtoImput, @AuthenticationPrincipal String username){
        return usuarioService.editarNombreDeUsuario(dtoImput, username);
    }

    // Anda
    @GetMapping("/get-all")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal String username){
        return usuarioService.getAll(username);
    }

    @GetMapping("/get/usuario")
    public ResponseEntity<?> getUsuarioByNombreUsuario(@AuthenticationPrincipal String username){
        
        return usuarioService.getUsuarioByNombreUsuarioConToken(username);
    }

    // Anda
    @GetMapping("/get-mensajes")
    public ResponseEntity<?> getMensajesUsuario(@AuthenticationPrincipal String username){
        return usuarioService.obtenerMensajes(username);
    }

    @PostMapping("/auth")
    public ResponseEntity<?> loginApi(@RequestBody LoginDtoInput dtoInput) {

        

        String username = dtoInput.getNombreDeUsuario();
        String password = dtoInput.getContrasenia();

        // Validación básica de credenciales
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Autenticar usuario usando el LoginService
        ResponseEntity<?> rta = usuarioService.iniciarSesion(username, password);

        if (!rta.getStatusCode().is2xxSuccessful()){
            return rta;
        }

        Rol rolUsuario = (Rol)rta.getBody();

        // Generar tokens
        String accessToken = JwtUtil.generarAccessToken(username);
        String refreshToken = JwtUtil.generarRefreshToken(username);

        AuthResponseDTO response = AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .rol(rolUsuario)
                .build();


        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenResponse request) {
        try {

            Claims claims = JwtUtil.parseClaims(request.getRefreshToken());

            if (!"refresh".equals(claims.get("type"))) {
                return ResponseEntity.badRequest().build();
            }

            ResponseEntity<?> rta = usuarioService.getUsuarioByNombreUsuario(claims.getSubject());

            if (!rta.getStatusCode().is2xxSuccessful()){
                return rta;
            }
            Usuario usuario = (Usuario) rta.getBody();
            Rol rol = usuario.getRol();
            String newAccessToken = JwtUtil.generarAccessToken(claims.getSubject());
            AuthResponseDTO response = AuthResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .rol(rol)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



}