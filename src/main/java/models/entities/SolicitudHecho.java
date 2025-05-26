package models.entities;

import models.entities.personas.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudHecho {
    private Usuario usuario;
    private Hecho hecho;
    private long id;
    private String justificacion;
    private boolean procesada;
    private boolean rechazadaPorSpam;

    // Constructor original (para agregar/modificar hechos)
    public SolicitudHecho(Usuario usuario, Hecho hecho, long id) {
        this.usuario = usuario;
        this.hecho = hecho;
        this.id = id;
        this.procesada = false;
        this.rechazadaPorSpam = false;
    }

    // Constructor adicional para eliminaciones con justificación
    public SolicitudHecho(Usuario usuario, Hecho hecho, long id, String justificacion) {
        this(usuario, hecho, id);
        this.justificacion = justificacion;
    }
}