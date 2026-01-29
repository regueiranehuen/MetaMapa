package com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import com.tpagrupo17.Metamapa.entities.DbDinamica.HechoDinamica;

@Entity
@DiscriminatorValue("SUBIR")
public class SolicitudSubirHecho extends SolicitudHecho{
    public SolicitudSubirHecho(Long usuario_id, HechoDinamica hecho) {
        this.usuario_id = usuario_id;
        this.hecho = hecho;
        this.procesada = false;
        this.rechazadaPorSpam = false;
    }

    public SolicitudSubirHecho(Long usuario_id, HechoDinamica hecho, String justificacion) {
        this(usuario_id, hecho);
        this.justificacion = justificacion;
    }

    public SolicitudSubirHecho() {

    }
}
