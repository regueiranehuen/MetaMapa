package com.tpagrupo17.Metamapa.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.atributosHecho.ContenidoMultimedia;

import java.util.List;

@Data
public class SolicitudHechoModificarInputDTO { //datos del hecho y el id del usuario
    @NotNull(message = "El id_hecho es obligatorio")
    private Long id_hecho; // Id del hecho que se quiere modificar
    private String titulo;
    private String descripcion;

    private String fechaAcontecimiento;
    private Double latitud;
    private Double longitud;
    private Long id_pais;
    private Long id_provincia;
    private Long id_categoria;

    private List<Long> contenidosMultimediaAEliminar;

    private List<ContenidoMultimedia> contenidosMultimedia;

    private List<ContenidoMultimediaDTO> nuevasRutasMultimedia;
}