package modulos.agregacion.controllers;

import lombok.AllArgsConstructor;
import modulos.agregacion.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categoria")
@AllArgsConstructor
public class CategoriaController {

    private CategoriaService categoriaService;

    @PostMapping("/add")
    public ResponseEntity<?> addCategoria(@AuthenticationPrincipal String username, @RequestParam String categoria){
        return categoriaService.addCategoria(username, categoria);
    }

    // Anda
    @PostMapping("/add/sinonimo/categoria")
    public ResponseEntity<?> addSinonimoCategoria(@AuthenticationPrincipal String username, @RequestParam Long id_categoria, @RequestParam String sinonimo){
        return categoriaService.addSinonimoCategoria(username, id_categoria, sinonimo);
    }
}
