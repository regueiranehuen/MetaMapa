package com.tpagrupo17.Metamapa.entities.usuario;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DatosPersonalesPublicador {

    private String nombre; // Campo obligatorio si se quiere subir un hecho

    private String apellido;

    private Integer edad;
}