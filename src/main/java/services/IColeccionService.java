package services;

import models.dtos.input.ColeccionInputDTO;
import models.entities.Coleccion;
import models.entities.DatosColeccion;
import models.entities.Hecho;
import models.entities.RespuestaHttp;
import models.entities.filtros.Filtro;
import models.entities.personas.Usuario;

import java.util.List;

public interface IColeccionService {
    //public RespuestaHttp<Void> crearColeccion(ColeccionInputDTO inputDTO);
    RespuestaHttp<Void> crearColeccion(ColeccionInputDTO inputDTO);
    List<Coleccion> obtenerTodasLasColecciones();
}

