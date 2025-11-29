package modulos.Front.dtos.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriosColeccionDTO {

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

    private List<String> categoria;
    private List<String> pais;
    private List<String> provincia;
}
