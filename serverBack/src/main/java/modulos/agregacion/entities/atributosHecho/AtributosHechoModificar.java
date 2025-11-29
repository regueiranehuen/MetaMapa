package modulos.agregacion.entities.atributosHecho;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;


@Data
@Table(name = "atributos_modificar_hecho")
@Entity
@Builder
@AllArgsConstructor
public class AtributosHechoModificar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "id_hechoModificar")
    private List<ContenidoMultimedia> contenidoMultimediaAgregar;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> contenidoMultimediaEliminar;

    @Column(name = "fechaAcontecimiento")
    private LocalDateTime fechaAcontecimiento;

    @Column(name = "ubicacion_id")
    private Long ubicacion_id;

    @Column(name = "categoria_id")
    private Long categoria_id;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    public AtributosHechoModificar() {

    }
}