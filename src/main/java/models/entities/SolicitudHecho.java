package models.entities;

import models.entities.personas.*;

import lombok.Getter;

@Getter
public class SolicitudHecho {
    private Usuario usuario;
    private Hecho hecho;
    private long id;
    private String justificacion;

    public SolicitudHecho(Usuario usuario, Hecho hecho, long id, String justificacion) {
        this.usuario = usuario;
        this.hecho = hecho;
        this.id = id;
        this.justificacion = justificacion;
    }
}
