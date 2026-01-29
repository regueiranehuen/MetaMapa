package com.tpagrupo17.Metamapa.entities.DbMain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatosColeccion {
    private String titulo;
    private String descripcion;

    public DatosColeccion(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }
}
