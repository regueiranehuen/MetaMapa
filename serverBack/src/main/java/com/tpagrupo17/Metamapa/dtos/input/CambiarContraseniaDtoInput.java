package com.tpagrupo17.Metamapa.dtos.input;

import lombok.Data;

@Data
public class CambiarContraseniaDtoInput {
    private String contrasenia_actual;
    private String contrasenia_nueva;
}
