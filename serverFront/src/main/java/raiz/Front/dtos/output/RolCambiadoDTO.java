package raiz.Front.dtos.output;

import lombok.Getter;
import lombok.Setter;
import raiz.Front.usuario.Rol;

@Getter
@Setter
public class RolCambiadoDTO {
    String username;
    Rol rol;
    Boolean rolModificado;
}
