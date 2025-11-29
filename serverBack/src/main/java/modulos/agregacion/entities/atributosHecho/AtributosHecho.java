
package modulos.agregacion.entities.atributosHecho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import modulos.agregacion.entities.DbMain.Fuente;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Embeddable
@Builder
@AllArgsConstructor
public class AtributosHecho {

    @Column(name = "titulo", columnDefinition = "VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_general_ci")
    private String titulo;

    @Column(name = "ubicacion_id")
    private Long ubicacion_id;

    @Column(name = "descripcion", columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci")
    private String descripcion;

    @Column(name = "fechaAcontecimiento")
    private LocalDateTime fechaAcontecimiento;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "id_hecho")
    private List<ContenidoMultimedia> contenidosMultimedia;

    @Column(name = "categoria_id")
    private Long categoria_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", length = 20)
    private Origen origen;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuente",length = 20)
    private Fuente fuente;

    @Column(name = "fechaCarga")
    private LocalDateTime fechaCarga;

    @Column(name = "fechaUltimaActualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    //se persiste porque si se corta la luz, se tiene que saber los hechos que estan modificados y todavia no revisados
    @Column(name = "modificado")
    private Boolean modificado;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    public AtributosHecho() {
    this.contenidosMultimedia = new ArrayList<>();
    }
}