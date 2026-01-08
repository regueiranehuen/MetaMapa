package raiz.Front.dtos.input;

import lombok.Data;

@Data
public class EditarUsuarioDtoInput {
    private String nombre;
    private String apellido;
    private Integer edad;
}
