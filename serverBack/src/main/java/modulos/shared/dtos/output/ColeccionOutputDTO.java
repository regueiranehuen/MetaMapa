package modulos.shared.dtos.output;

import lombok.Data;
import modulos.shared.dtos.input.CriteriosColeccionDTO;

import java.util.List;

@Data
public class ColeccionOutputDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private List<String> datasets;

    private String algoritmoDeConsenso;
    private CriteriosColeccionDTO criterios;
}
