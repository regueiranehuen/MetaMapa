package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Data;
import com.tpagrupo17.Metamapa.entities.ContenidoMultimedia;

import java.util.List;

@Data
public class AtributosModificarDTO {
    private String username;
    private String fuente;
    private String titulo;
    private String descripcion;
    private Long id_categoria;
    private String categoria;
    private Long id_pais;
    private String pais;
    private Long id_provincia;
    private String provincia;
    private String fechaAcontecimiento;
    private String fechaCarga;
    private Double latitud;
    private Double longitud;
    private List<ContenidoMultimedia> contenido;
}

