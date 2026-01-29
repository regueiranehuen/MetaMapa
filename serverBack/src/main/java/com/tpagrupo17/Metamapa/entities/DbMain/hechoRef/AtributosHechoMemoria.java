package com.tpagrupo17.Metamapa.entities.DbMain.hechoRef;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.DbMain.Ubicacion;
import com.tpagrupo17.Metamapa.entities.DbMain.Categoria;
import com.tpagrupo17.Metamapa.entities.atributosHecho.ContenidoMultimedia;
import com.tpagrupo17.Metamapa.entities.atributosHecho.Origen;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AtributosHechoMemoria {

    private String titulo;

    private Ubicacion ubicacion;

    private String descripcion;

    private LocalDateTime fechaAcontecimiento;

    private List<ContenidoMultimedia> contenidoMultimedia;

    private Categoria categoria;

    private Origen origen;

    private LocalDateTime fechaCarga;

    private LocalDateTime fechaUltimaActualizacion;

    private Boolean modificado;

    private Double latitud;

    private Double longitud;

    public AtributosHechoMemoria(){}

}
