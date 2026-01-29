package com.tpagrupo17.Metamapa.entities.usuario;

import lombok.Data;

@Data

public class Usuario {

    private String contrasenia;

    private Long id;

    private String nombreDeUsuario;

    private Integer cantHechosSubidos;

    private Rol rol; // 0 visualizador, 1 contribuyente, 2 admin

    private DatosPersonalesPublicador datosPersonales;


}