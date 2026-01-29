package com.tpagrupo17.Metamapa.dtos.input;

import lombok.Data;

@Data
public class SinonimoInputDTO {
    private String tipo;

    private Long id_entidad;

    private Long id_pais; //solo se rellena si el tipo es provincia

    private String sinonimo;
}