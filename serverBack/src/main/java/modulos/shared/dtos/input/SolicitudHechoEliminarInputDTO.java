package modulos.shared.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Size;


@Data
public class SolicitudHechoEliminarInputDTO {

    @NotNull(message = "El id_hecho es obligatorio")
    Long id_hecho;
    @NotNull(message = "La justificación es obligatoria")
    @Size(min = 500, max = 1001, message = "La justificación debe ser de entre 500 y 1000 caracteres")
    String justificacion;
}
