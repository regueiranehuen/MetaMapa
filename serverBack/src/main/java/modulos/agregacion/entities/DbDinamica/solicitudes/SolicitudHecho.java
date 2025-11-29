package modulos.agregacion.entities.DbDinamica.solicitudes;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbMain.usuario.Usuario;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "solicitud_hecho")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_solicitud", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class SolicitudHecho {

    @Column(name = "usuario_id")
    protected Long usuario_id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_hecho", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_solicitudHecho_hecho"))
    protected HechoDinamica hecho;
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    protected long id;
    @Column (name = "justificacion", length = 1000)
    protected String justificacion;
    @Column (name = "procesada")
    protected boolean procesada;
    @Column (name = "rechazadaPorSpam")
    protected boolean rechazadaPorSpam;

    @Column (name = "fecha")
    protected LocalDateTime fecha;

    public SolicitudHecho(Long usuario_id, HechoDinamica hecho) {
        this.usuario_id = usuario_id;
        this.hecho = hecho;
        this.procesada = false;
        this.rechazadaPorSpam = false;
    }

    public SolicitudHecho(Long usuario_id, HechoDinamica hecho, String justificacion) {
        this(usuario_id, hecho);
        this.justificacion = justificacion;
    }

    public SolicitudHecho() {

    }
}
