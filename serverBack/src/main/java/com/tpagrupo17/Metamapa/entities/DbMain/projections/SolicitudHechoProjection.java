package com.tpagrupo17.Metamapa.entities.DbMain.projections;

public interface SolicitudHechoProjection {
    Long getId();
    Long getUsuarioId();
    Long getHechoId();
    String getJustificacion();
    Boolean getProcesada();
    Boolean getRechazadaPorSpam();
}
