package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDtoInput {
    @NotNull(message = "Ingrese contraseña")
    private String contrasenia;
    @NotNull(message = "Ingrese nombre de usuario")
    private String nombreDeUsuario;
}
