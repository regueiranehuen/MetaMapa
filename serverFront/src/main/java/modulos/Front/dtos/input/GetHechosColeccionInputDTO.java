package modulos.Front.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GetHechosColeccionInputDTO {
    private List<Long> categoriaId;
    private List<Integer> contenidoMultimedia;
    private String descripcion;
    private String fechaAcontecimientoInicial;
    private String fechaAcontecimientoFinal;
    private String fechaCargaInicial;
    private String fechaCargaFinal;
    private List<Integer> fuentes;
    private List<Long> paisId;
    private String titulo;
    private List<Long> provinciaId;
    private Integer origenConexion;

    private List<String> categoria;
    private List<String> pais;
    private List<String> provincia;

    @NotNull(message = "La forma de navegación debe ser especificada")
    private Boolean navegacionCurada;

    @NotNull(message = "El id_coleccion es obligatorio")
    private Long id_coleccion;
}
