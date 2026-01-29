package com.tpagrupo17.Metamapa.entities.fuentes;

import com.tpagrupo17.Metamapa.entities.DbDinamica.HechoDinamica;
import com.tpagrupo17.Metamapa.entities.atributosHecho.ContenidoMultimedia;
import com.tpagrupo17.Metamapa.entities.atributosHecho.Origen;
import com.tpagrupo17.Metamapa.dtos.input.SolicitudHechoInputDTO;
import com.tpagrupo17.Metamapa.utils.FechaParser;

import java.time.LocalDateTime;
import java.util.List;


public class FuenteDinamica {

    public HechoDinamica crearHecho(SolicitudHechoInputDTO data, List<ContenidoMultimedia> contenidosMultimedia, Long id_categoria, Long id_ubicacion){

        HechoDinamica hecho = new HechoDinamica();
        hecho.getAtributosHecho().setTitulo(data.getTitulo());
        hecho.setActivo(false);
        hecho.getAtributosHecho().setDescripcion(data.getDescripcion());
        hecho.getAtributosHecho().setContenidosMultimedia(contenidosMultimedia);
        LocalDateTime fecha = FechaParser.parsearFecha(data.getFechaAcontecimiento());
        hecho.getAtributosHecho().setFechaAcontecimiento(fecha);
        hecho.getAtributosHecho().setUbicacion_id(id_ubicacion);
        hecho.getAtributosHecho().setOrigen(Origen.FUENTE_DINAMICA);
        hecho.getAtributosHecho().setModificado(true);
        hecho.getAtributosHecho().setCategoria_id(id_categoria);
        hecho.getAtributosHecho().setLatitud(data.getLatitud());
        hecho.getAtributosHecho().setLongitud(data.getLongitud());
        return hecho;
    }
}
