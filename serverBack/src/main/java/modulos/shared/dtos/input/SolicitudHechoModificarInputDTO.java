package modulos.shared.dtos.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import modulos.agregacion.entities.atributosHecho.ContenidoMultimedia;
import org.springframework.web.multipart.MultipartFile;

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


    //private List<MultipartFile> contenidosMultimediaParaAgregar;
    private List<Long> contenidosMultimediaAEliminar;

    private List<ContenidoMultimedia> contenidosMultimedia;

    private List<ContenidoMultimediaDTO> nuevasRutasMultimedia;
}