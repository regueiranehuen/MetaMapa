package modulos.Front.dtos.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import modulos.Front.ContenidoMultimedia;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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