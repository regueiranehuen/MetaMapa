package modulos.Front.dtos.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
public class SolicitudHechoInputDTO { //datos del hecho y el id del usuario


    private String titulo;

    private String descripcion;
    private String fechaAcontecimiento;

    private Double latitud;
    private Double longitud;

    private Long id_pais;
    private Long id_categoria;
    private Long id_provincia;
    @JsonIgnore
    private List<MultipartFile> contenidosMultimedia;
}
