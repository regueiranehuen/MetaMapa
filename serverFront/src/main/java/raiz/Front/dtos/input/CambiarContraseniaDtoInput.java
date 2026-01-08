package raiz.Front.dtos.input;

import lombok.Data;

@Data
public class CambiarContraseniaDtoInput {
    private String contrasenia_actual;
    private String contrasenia_nueva;
}
