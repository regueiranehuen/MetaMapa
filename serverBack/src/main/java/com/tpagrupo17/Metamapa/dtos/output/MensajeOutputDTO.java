package com.tpagrupo17.Metamapa.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MensajeOutputDTO {
    private Long id_usuario;
    private Long id_solicitud_hecho;
    private Long id_mensaje;
    private String mensaje;
 }
