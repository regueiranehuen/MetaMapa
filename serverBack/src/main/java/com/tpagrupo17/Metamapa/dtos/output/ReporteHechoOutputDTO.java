package com.tpagrupo17.Metamapa.dtos.output;

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
