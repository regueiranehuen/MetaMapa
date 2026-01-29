package com.tpagrupo17.Metamapa.dtos;

import lombok.Data;
import com.tpagrupo17.Metamapa.dtos.output.CategoriaDto;
import com.tpagrupo17.Metamapa.dtos.output.ColeccionOutputDTO;
import com.tpagrupo17.Metamapa.dtos.output.PaisDto;

import java.util.List;
@Data
public class DetallesColeccionCargadaDTO
{
    private ColeccionOutputDTO coleccionDto;
    private String descripcion;
    private String fechaAcontecimientoInicial;
    private String fechaAcontecimientoFinal;
    private String fechaCargaInicial;
    private String fechaCargaFinal;
    private String titulo;
    private List<CategoriaDto> categoria;
    private List<PaisDto> pais;
    private List<ContenidoMultimediaDto> contenidoMultimedia;
    private List<OrigenDto> virgenes;
    private Boolean navegacionCurada;
}
