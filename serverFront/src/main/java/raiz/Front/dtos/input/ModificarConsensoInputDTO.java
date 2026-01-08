package raiz.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModificarConsensoInputDTO {
    private Long idColeccion;
    @NotNull(message = "Debe especificarse el tipo de consenso")
    private String tipoConsenso; //multiples menciones, mayría simple, mayoría absoluta
}
