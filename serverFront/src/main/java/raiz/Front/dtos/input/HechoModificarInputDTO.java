package raiz.Front.dtos.input;

import lombok.Builder;
import lombok.Data;
import raiz.Front.ContenidoMultimedia;

import java.util.List;

@Data
@Builder
public class HechoModificarInputDTO {
    private Long id_hecho;
    private String titulo;
    private String descripcion;
    private String fechaAcontecimiento;
    private Double latitud;
    private Double longitud;
    private Long id_pais;
    private Long id_provincia;
    private Long id_categoria;


    private List<Long> contenidosMultimediaAEliminar;
    private String fuente;

    // 👉 NUEVO: esto sí se manda al back
    private List<ContenidoMultimediaDTO> nuevasRutasMultimedia;

    private List<ContenidoMultimedia> contenidosMultimedia;
}