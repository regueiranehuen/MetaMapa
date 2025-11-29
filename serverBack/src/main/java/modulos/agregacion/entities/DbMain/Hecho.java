package modulos.agregacion.entities.DbMain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.entities.atributosHecho.AtributosHechoModificar;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@MappedSuperclass
@AllArgsConstructor
public abstract class Hecho {

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuario_id;

    @Embedded
    private AtributosHecho atributosHecho;

    @Column(name = "cant_accesos")
    private Long cant_accesos;

    @PrePersist
    protected void onCreate() {
        if (cant_accesos == null) {
            cant_accesos = 0L;
        }
    }

    public Hecho() {
        this.atributosHecho = new AtributosHecho();
        this.activo = false;
    }

    public void incrementarAccesos(){
        this.cant_accesos++;
    }
}


/*


Many To One



metodo falopa(){

hecho1

hecho1.setNombre("lucca);

save()
)
}

* PERSIST → si persisto el padre, persiste también el hijo.

MERGE → si actualizo el padre, mergea también el hijo.

REMOVE → si borro el padre, borra también el hijo.

REFRESH → si refresco el padre, refresca también el hijo.

DETACH → si saco al padre del contexto (detach), también el hijo.

ALL → equivale a todas las anteriores.

* cascade = {PERSIST, MERGE}
* * */

/*
* HECHO
* FK A UBICACION: 1
*
* UBICACION:
* ID: 1
* FK A PAIS: 1
* FK A PROVINCIA: 1
*
* ID 2
* FK A PAIS: 1
* FK A PROVINCIA: 2
*
* */
