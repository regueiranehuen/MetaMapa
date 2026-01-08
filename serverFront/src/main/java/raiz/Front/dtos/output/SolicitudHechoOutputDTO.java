package raiz.Front.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SolicitudHechoOutputDTO {
    private Long id;
    private Long usuarioId;
    private Long hechoId;
    private String username;
    private String justificacion;
    private Boolean procesada;
    private Boolean rechazadaPorSpam;
    private String fecha;
    private String tipo;

    public SolicitudHechoOutputDTO() {}
}
