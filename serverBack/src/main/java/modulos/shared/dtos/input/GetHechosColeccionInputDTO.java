package modulos.shared.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import modulos.agregacion.entities.atributosHecho.OrigenConexion;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
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
