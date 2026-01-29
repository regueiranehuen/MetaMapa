package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
}