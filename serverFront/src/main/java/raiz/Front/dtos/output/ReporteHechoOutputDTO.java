package raiz.Front.dtos.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReporteHechoOutputDTO {
    private Long id;
    private String motivo;
    private Long id_hecho;
    private String fuente;
}
