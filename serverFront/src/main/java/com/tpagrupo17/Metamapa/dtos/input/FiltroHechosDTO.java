package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FiltroHechosDTO {

    private CriteriosColeccionDTO criterios;

    @NotNull(message = "El id_coleccion es obligatorio")
    private Long id_coleccion;
}
