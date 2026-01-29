package com.tpagrupo17.Metamapa.dtos.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.ContenidoMultimedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
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


    @JsonIgnore
    private List<MultipartFile> contenidosMultimediaParaAgregar;
    private List<Long> contenidosMultimediaAEliminar;


    // 👉 NUEVO: esto sí se manda al back
    private List<ContenidoMultimediaDTO> nuevasRutasMultimedia;

    private List<ContenidoMultimedia> contenidosMultimedia;
}