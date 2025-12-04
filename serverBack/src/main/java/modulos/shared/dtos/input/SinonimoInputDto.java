package modulos.shared.dtos.input;

import lombok.Data;

@Data
public class SinonimoInputDto {
    private String tipo;

    private Long id_entidad;

    private Long id_pais; //solo se rellena si el tipo es provincia

    private String sinonimo;
}
