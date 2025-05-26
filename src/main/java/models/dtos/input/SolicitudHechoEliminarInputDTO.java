package models.dtos.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;


@Data
public class SolicitudHechoEliminarInputDTO {

    @NotNull(message = "El id_usuario es obligatorio")
    Long id_usuario;

    @NotNull(message = "El id_hecho es obligatorio")
    Long id_hecho;

    @NotNull(message = "La justificación es obligatoria")
    @Size(min = 500, message = "La justificación debe tener al menos 500 caracteres")
    String justificacion;
}
