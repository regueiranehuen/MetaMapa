package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColeccionUpdateInputDTO {
    @NotNull(message = "el id_coleccion es obligatorio")
    private Long id_coleccion;

    private String titulo;
    private String descripcion;

    private CriteriosColeccionDTO criterios;

    // Algoritmo de consenso (opcional)
    private String algoritmoConsenso;
}
