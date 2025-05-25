package services.impl;

import models.dtos.input.ColeccionInputDTO;
import models.entities.*;
import models.entities.buscadores.BuscadorCategoria;
import models.entities.buscadores.BuscadorPais;
import models.entities.filtros.*;
import models.entities.personas.Rol;
import models.entities.personas.Usuario;
import models.repositories.IColeccionRepository;
import models.repositories.IHechosRepository;
import models.repositories.IPersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import services.IColeccionService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ColeccionService implements IColeccionService {

    private final IHechosRepository hechosRepo;
    private final IColeccionRepository coleccionesRepo;
    private final IPersonaRepository usuariosRepo;

    public ColeccionService(IHechosRepository hechosRepo, IColeccionRepository coleccionesRepo, IPersonaRepository usuariosRepo) {
        this.hechosRepo = hechosRepo;
        this.coleccionesRepo = coleccionesRepo;
        this.usuariosRepo = usuariosRepo;
    }


    /*
    Colecciones
Las colecciones representan conjuntos de hechos. Las mismas pueden ser consultadas por cualquier persona, de forma
pública, y no pueden ser editadas ni eliminadas manualmente (esto último, con una sola excepción, ver más adelante).

Las colecciones tienen un título, como por ejemplo “Desapariciones vinculadas a crímenes de odio”, o “Incendios
forestales en Argentina 2025” y una descripción. Las personas administradoras pueden crear tantas colecciones como deseen.

Las colecciones están asociadas a una fuente y tomarán los hechos de las mismas: para esto las colecciones también contarán con un criterio de
pertenencia configurable, que dictará si un hecho pertenece o no a las mismas. Por ejemplo, la colección de “Incendios forestales…” deberá
incluir automáticamente todos los hechos de categoría “Incendio forestal” ocurrido en Argentina, acontecido entre el 1 de enero de 2025 a las
0:00 y el 31 de diciembre de 20205 a las 23:59.

    */

    @Override
    public RespuestaHttp<Void> crearColeccion(ColeccionInputDTO dtoInput) {

        Usuario usuario = usuariosRepo.findById(dtoInput.getId_usuario());

        if (usuario == null || !usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return new RespuestaHttp<>(null, HttpStatus.UNAUTHORIZED.value());
        }

        DatosColeccion datosColeccion = new DatosColeccion(dtoInput.getTitulo(), dtoInput.getDescripcion(), dtoInput.getFuente());

        Coleccion coleccion = new Coleccion(datosColeccion,coleccionesRepo.getProxId());

        List<Filtro> criterios = new ArrayList<>();


        if(dtoInput.getPais() != null) {
            Pais pais = BuscadorPais.buscar(hechosRepo.findAll(),dtoInput.getPais());
            Filtro filtroPais = new FiltroPais(pais);
            criterios.add(filtroPais);
        }
        if(dtoInput.getFechaAcontecimientoFinal() != null && dtoInput.getFechaAcontecimientoInicial() != null) {
            Filtro filtroFechaAcontecimiento = new FiltroFechaAcontecimiento(FechaParser.parsearFecha(dtoInput.getFechaAcontecimientoInicial()),FechaParser.parsearFecha(dtoInput.getFechaAcontecimientoFinal()));
            criterios.add(filtroFechaAcontecimiento);
        }
        if(dtoInput.getContenidoMultimedia() != null) {
            Filtro filtroContenidoMultimedia = new FiltroContenidoMultimedia(TipoContenido.fromCodigo(dtoInput.getContenidoMultimedia()));
            criterios.add(filtroContenidoMultimedia);
        }
        if(dtoInput.getCategoria() != null){
            Categoria categoria = BuscadorCategoria.buscar(hechosRepo.findAll(),dtoInput.getCategoria());
            Filtro filtroCategoria = new FiltroCategoria(categoria);
            criterios.add(filtroCategoria);
        }
        if (dtoInput.getFechaCargaInicial()!=null && dtoInput.getFechaCargaFinal() !=null){
            Filtro filtroFechaCarga = new FiltroFechaAcontecimiento(FechaParser.parsearFecha(dtoInput.getFechaCargaInicial()), FechaParser.parsearFecha(dtoInput.getFechaCargaFinal()));
        }

        coleccion.addCriterios(criterios);
        List<Hecho> hechos = hechosRepo.findAll();
        Filtrador filtrador = new Filtrador();

        coleccion.addHechos(filtrador.aplicarFiltros(criterios, hechos));

        coleccionesRepo.save(coleccion);

        return new RespuestaHttp<>(null, HttpStatus.OK.value());

    }

    @Override
    public List<Coleccion> obtenerTodasLasColecciones() {
        return coleccionesRepo.findAll();
    }

}



//TODO Cada vez que se crea un hecho que se meta el en las colecciones que el hecho cumple su criterio
