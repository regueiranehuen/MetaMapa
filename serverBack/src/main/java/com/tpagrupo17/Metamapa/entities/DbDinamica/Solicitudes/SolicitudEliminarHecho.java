package com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import com.tpagrupo17.Metamapa.entities.DbDinamica.HechoDinamica;

@Entity
@DiscriminatorValue("ELIMINAR")
public class SolicitudEliminarHecho extends SolicitudHecho{
    public SolicitudEliminarHecho(Long usuario_id, HechoDinamica hecho) {
        this.usuario_id = usuario_id;
        this.hecho = hecho;
        this.procesada = false;
        this.rechazadaPorSpam = false;
    }

    public SolicitudEliminarHecho(Long usuario_id, HechoDinamica hecho, String justificacion) {
        this(usuario_id, hecho);
        this.justificacion = justificacion;
    }

    public SolicitudEliminarHecho() {

    }
}
