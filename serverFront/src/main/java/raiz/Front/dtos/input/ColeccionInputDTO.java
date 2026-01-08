package raiz.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColeccionInputDTO {
    @NotNull(message = "El campo titulo es obligatorio")
    private String titulo;
    @NotNull(message = "La descripción es obligatoria")
    private String descripcion;

    //Criterios (es decir los filtros)
    private CriteriosColeccionDTO criterios;

    // Algoritmo de consenso (opcional)
    private String algoritmoConsenso;

    public ColeccionInputDTO() {
        this.criterios = new CriteriosColeccionDTO();
    }

}
