package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Nombre único dato obligatorio (entrega 1)
@Data
public class UsuarioInputDTO {

    @NotNull(message = "Campo nombre obligatorio")
    private String nombre;

    @NotNull(message = "Campo nombre usuario obligatorio")
    private String nombreUsuario;

    private String apellido;

    private Integer edad;
    @NotNull(message = "Campo contraseña obligatorio")
    private String contrasenia;
}
