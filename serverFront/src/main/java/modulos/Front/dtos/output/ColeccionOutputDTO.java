package modulos.Front.dtos.output;

import lombok.Data;
import modulos.Front.dtos.input.CriteriosColeccionDTO;
import modulos.Front.dtos.input.ProxyDTO;

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
