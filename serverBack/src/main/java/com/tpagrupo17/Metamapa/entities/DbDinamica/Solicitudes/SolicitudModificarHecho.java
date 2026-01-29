package com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.atributosHecho.AtributosHechoModificar;
import com.tpagrupo17.Metamapa.entities.DbDinamica.HechoDinamica;

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


