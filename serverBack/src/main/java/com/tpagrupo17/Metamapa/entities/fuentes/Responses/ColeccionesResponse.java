package com.tpagrupo17.Metamapa.entities.fuentes.Responses;

import lombok.Data;
import com.tpagrupo17.Metamapa.dtos.output.ColeccionOutputDTO;

import java.util.List;

@Data
public class ColeccionesResponse {
private List<ColeccionOutputDTO> colecciones;
}
