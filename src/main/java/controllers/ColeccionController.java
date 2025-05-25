package controllers;

import jakarta.validation.Valid;
import models.dtos.input.ColeccionInputDTO;
import models.dtos.output.VisualizarHechosOutputDTO;
import models.entities.RespuestaHttp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.IColeccionService;
import models.entities.Coleccion;


import java.util.List;

@RestController
@RequestMapping("/api/coleccion")
@CrossOrigin(origins = "http://localhost:3000")
public class ColeccionController {

    private final IColeccionService coleccionService;

    public ColeccionController(IColeccionService coleccionService){
        this.coleccionService = coleccionService;
    }
    // TODO lo relacionado a colecciones


    @GetMapping
    public ResponseEntity<List<Coleccion>> obtenerTodasLasColecciones() {
     List<Coleccion> colecciones = coleccionService.obtenerTodasLasColecciones();
     return ResponseEntity.ok(colecciones);
    }

    @PostMapping("/crear")
    public ResponseEntity<Void> crearColeccion(@Valid @RequestBody ColeccionInputDTO inputDTO){
        RespuestaHttp<Void> respuesta = coleccionService.crearColeccion(inputDTO);
        return ResponseEntity.status(respuesta.getCodigo()).build();
    }

}
