package com.tpagrupo17.Metamapa.entities.DbMain.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class DatosPersonalesPublicador {
    @Column (name = "nombre")
    private String nombre; // Campo obligatorio si se quiere subir un hecho
    @Column (name = "apellido")
    private String apellido;
    @Column (name = "edad")
    private Integer edad;
}