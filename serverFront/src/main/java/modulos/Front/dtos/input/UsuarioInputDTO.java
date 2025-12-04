package modulos.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
