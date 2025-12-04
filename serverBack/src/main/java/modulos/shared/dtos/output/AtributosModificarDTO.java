package modulos.shared.dtos.output;

import lombok.Builder;
import lombok.Data;
import modulos.agregacion.entities.atributosHecho.ContenidoMultimedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class AtributosModificarDTO {
    private String username;
    private String fuente;
    private String titulo;
    private String descripcion;
    private Long id_categoria;
    private String categoria;
    private Long id_pais;
    private String pais;
    private Long id_provincia;
    private String provincia;
    private String fechaAcontecimiento;
    private String fechaCarga;
    private Double latitud;
    private Double longitud;
    private List<ContenidoMultimedia> contenido;
}