package com.tpagrupo17.Metamapa.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.DbMain.Categoria;
import com.tpagrupo17.Metamapa.entities.DbMain.Ubicacion;
import com.tpagrupo17.Metamapa.entities.atributosHecho.ContenidoMultimedia;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class AtributosHechoModificarMemoria {

    private Long id;

    private String titulo;

    private String descripcion;

    private List<ContenidoMultimedia> contenidoMultimediaAgregar;

    private List<Long> contenidoMultimediaEliminar;

    private ZonedDateTime fechaAcontecimiento;

    private Ubicacion ubicacion;

    private Categoria categoria;

    private Double latitud;

    private Double longitud;
    public AtributosHechoModificarMemoria(){}
}
