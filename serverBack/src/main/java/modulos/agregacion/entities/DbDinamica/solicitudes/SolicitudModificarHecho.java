package modulos.agregacion.entities.DbDinamica.solicitudes;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.atributosHecho.AtributosHechoModificar;
import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
@Entity
@DiscriminatorValue("MODIFICAR")
@Setter
@Getter
public class SolicitudModificarHecho extends SolicitudHecho{
    public SolicitudModificarHecho(Long usuario_id, HechoDinamica hecho) {
        this.usuario_id = usuario_id;
        this.hecho = hecho;
        this.procesada = false;
        this.rechazadaPorSpam = false;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AtributosHechoModificar atributosModificar;

    public SolicitudModificarHecho() {
    }
}


