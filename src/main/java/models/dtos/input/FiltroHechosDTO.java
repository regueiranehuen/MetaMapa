package models.dtos.input;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Setter;

@Data
public class FiltroHechosDTO {

    @Setter
    private String categoria;
    private String contenidoMultimedia;
    private String descripcion;
    private String fechaAcontecimientoInicial;
    private String fechaAcontecimientoFinal;
    private String fechaCargaInicial;
    private String fechaCargaFinal;
    private String origen;
    private String pais;
    private String titulo;

    // hicimos los setters para que los pueda agarrar el controlador y asignamos los nombres de los
    // atributos acorde lo pide el anunciado como fechaAcontecimientoInicial = fechaAcontecimientoDesde
    public void setFechaReporteDesde(String fechaReporteDesde) { this.fechaCargaInicial = fechaReporteDesde; }
    public void setFechaReporteHasta(String fechaReporteHasta) { this.fechaCargaFinal = fechaReporteHasta; }
    public void setFechaAcontecimientoDesde(String fechaAcontecimientoDesde) { this.fechaAcontecimientoInicial = fechaAcontecimientoDesde; }
    public void setFechaAcontecimientoHasta(String fechaAcontecimientoHasta) { this.fechaAcontecimientoFinal = fechaAcontecimientoHasta; }
    public void setUbicacion(String ubicacion) { this.origen = ubicacion; }

    @NotNull(message = "El id_coleccion es obligatorio")
    private Long id_coleccion;

}
