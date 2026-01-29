package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SolicitudHechoEvaluarInputDTO {
    @NotNull(message = "Debe seleccionar una opción")
    private Boolean respuesta;
    @NotNull(message = "El id_solicitud es obligatorio")
    private Long id_solicitud;

    private String mensaje;
}



// Usuario -> solicitud (datos del hecho) -> crear instancia SolicitudHecho (usuario,hecho) -> valido = false
//Administrador -> evaluarsolicitud(id_solicitud,respuesta) -> if(respuesta) -> valido = true
