package com.tpagrupo17.Metamapa.entities.fuentes.Responses;

import lombok.Data;

@Data
public class HechoResponse {
    private String titulo;
    private String descripcion;
    private String categoria;
    private Double latitud;
    private Double longitud;
    private String fecha_hecho;
    private String created_at;
    private String updated_at;
}
