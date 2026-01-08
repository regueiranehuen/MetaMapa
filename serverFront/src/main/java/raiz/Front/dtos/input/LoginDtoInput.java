package raiz.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDtoInput {
    @NotNull(message = "Ingrese contraseña")
    private String contrasenia;
    @NotNull(message = "Ingrese nombre de usuario")
    private String nombreDeUsuario;
}
