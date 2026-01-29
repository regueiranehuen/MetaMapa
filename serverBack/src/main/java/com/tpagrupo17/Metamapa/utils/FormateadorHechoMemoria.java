package com.tpagrupo17.Metamapa.utils;

import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.AtributosHechoMemoria;
import com.tpagrupo17.Metamapa.entities.DbEstatica.HechoEstatica;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;
import com.tpagrupo17.Metamapa.entities.HechoMemoria;
import com.tpagrupo17.Metamapa.buscadores.BuscadoresRegistry;
import org.springframework.stereotype.Component;


//AURA
@Component
public class FormateadorHechoMemoria {
    private final BuscadoresRegistry buscadores;
    public FormateadorHechoMemoria(BuscadoresRegistry buscadores) {
        this.buscadores = buscadores;
    }
    public HechoMemoria formatearHechoMemoria(Hecho hecho){

        AtributosHechoMemoria atributos = AtributosHechoMemoria.builder().
                categoria(buscadores.getBuscadorCategoria().buscar(hecho.getAtributosHecho().getCategoria_id()))
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud()).longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidoMultimedia(hecho.getAtributosHecho().getContenidosMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion(buscadores.getBuscadorUbicacion().buscarUbicacion(hecho.getAtributosHecho().getUbicacion_id()))
                .build();

        //List<AtributosHechoModificar> listaAtributosHechoModificar = hecho.getAtributosHechoAModificar();

        HechoMemoria hecho123 = HechoMemoria.builder().
                id(hecho.getId()).
                activo(hecho.getActivo()).
                usuario_id(hecho.getUsuario_id()).
                atributosHecho(atributos).
                //atributosHechoAModificar(listaAtributosHechoModificar).
                build();

        if(hecho instanceof HechoEstatica hechoEstatica){
            hecho123.setDatasets(hechoEstatica.getDatasets());
        }

    return hecho123;
    }

}
