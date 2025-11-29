package modulos.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColeccionUpdateInputDTO {
    @NotNull(message = "el id_coleccion es obligatorio")
    private Long id_coleccion;

    private String titulo;
    private String descripcion;

    private CriteriosColeccionDTO criterios;

    // Algoritmo de consenso (opcional)
    private String algoritmoConsenso;
}
