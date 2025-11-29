package modulos.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SolicitudHechoEliminarInputDTO {
    @NotNull(message = "El id_hecho es obligatorio")
    Long id_hecho;
    @NotNull(message = "La justificación es obligatoria")
    @Size(min = 500, max = 1001, message = "La justificación debe ser de entre 500 y 1000 caracteres")
    String justificacion;
}
