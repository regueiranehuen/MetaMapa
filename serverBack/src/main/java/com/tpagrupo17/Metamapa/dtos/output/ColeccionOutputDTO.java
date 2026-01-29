package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Data;
import com.tpagrupo17.Metamapa.dtos.input.CriteriosColeccionDTO;

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
