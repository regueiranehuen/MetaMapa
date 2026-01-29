package com.tpagrupo17.Metamapa.entities.fuentes.Responses;

import lombok.Data;
import com.tpagrupo17.Metamapa.entities.atributosHecho.ContenidoMultimedia;

import java.util.List;

@Data
public class HechoMetamapaResponse {
    private Long id;
    private String fuente;
    private String titulo;
    private String descripcion;
    private String categoria;
    private String pais;
    private String provincia;
    private String fechaAcontecimiento;
    private Double latitud;
    private Double longitud;
    private List<ContenidoMultimedia> contenido;
}
