package modulos.agregacion.controllers;

import io.jsonwebtoken.Jwt;
import jakarta.validation.Valid;
import modulos.BodyToListConverter;
import modulos.agregacion.services.ColeccionService;
import modulos.shared.dtos.input.ColeccionInputDTO;
import modulos.shared.dtos.input.ColeccionUpdateInputDTO;
import modulos.shared.dtos.input.ModificarConsensoInputDTO;
import modulos.shared.dtos.output.ColeccionOutputDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coleccion")
public class ColeccionController {

    private final ColeccionService coleccionService;

    public ColeccionController(ColeccionService coleccionService){
        this.coleccionService = coleccionService;
    }

    @GetMapping("/public/cantColecciones")
    public ResponseEntity<?> getCantColecciones(){
        return coleccionService.getCantColecciones();
    }

    //anda
    @PostMapping("/crear")
    public ResponseEntity<?> crearColeccion(@Valid @RequestBody ColeccionInputDTO inputDTO, @AuthenticationPrincipal String username){
            System.out.println("SOY EL USUARIO: " + username);
            return coleccionService.crearColeccion(inputDTO, username); // 201 o 401
    }

    //anda
    @GetMapping("/public/get-all")
    public ResponseEntity<?> obtenerTodasLasColecciones() {
        return coleccionService.obtenerTodasLasColecciones();
    }

    //anda
    @GetMapping("/public/get/{id_coleccion}")
    public ResponseEntity<?> getColeccion(@PathVariable Long id_coleccion){
        return coleccionService.getColeccion(id_coleccion);
    }
    //anda
    @PostMapping("/delete/{id_coleccion}")
    public ResponseEntity<?> deleteColeccion(@PathVariable Long id_coleccion, @AuthenticationPrincipal String username){
        return coleccionService.deleteColeccion(id_coleccion, username);
    }
    //anda
    @PostMapping("/update")
    public ResponseEntity<?> updateColeccion(@Valid @RequestBody ColeccionUpdateInputDTO dto, @AuthenticationPrincipal String username){
        return coleccionService.updateColeccion(dto, username);
    }

    @PostMapping("/add/fuente")
    public ResponseEntity<?> agregarFuente(@Valid @RequestParam Long id_coleccion, @Valid @RequestParam String dataset, @AuthenticationPrincipal Jwt principal){
        return coleccionService.agregarFuente(id_coleccion,dataset, principal);
    }

    @PostMapping("/delete/fuente")
    public ResponseEntity<?> eliminarFuente(@Valid @RequestParam Long id_coleccion, @Valid @RequestParam Long id_dataset, @AuthenticationPrincipal Jwt principal){
        return coleccionService.eliminarFuente(id_coleccion, id_dataset, principal);
    }

    @PostMapping("/colecciones/modificar-consenso")
    public ResponseEntity<?> modificarAlgoritmoConsenso(@RequestBody ModificarConsensoInputDTO input, @AuthenticationPrincipal Jwt principal) {
        return coleccionService.modificarAlgoritmoConsenso(input, principal);
    }

    @PostMapping("/refrescar")
    public ResponseEntity<?> refrescarColecciones(@AuthenticationPrincipal Jwt principal){
        return coleccionService.refrescarColecciones(principal);
    }

    @GetMapping("/public/destacadas")
    public ResponseEntity<?> getColeccionesDestacadas(){
        System.out.println("Entro a colecciones destacadas");
        return coleccionService.getColeccionDestacados();
    }

}

