package com.tpagrupo17.Metamapa.entities.DbMain.hechoRef;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.DbMain.Fuente;

@Entity
@Table(name = "hecho_ref")
@Getter
@Setter

// NOTA: HechoRef se usa en las relaciones de la db main en la que debe guardarse una referencia a uno o varios hechos -> Se usa en colecciones y en reporte
public class HechoRef {

    // PK compuesta (id_hecho, fuente)
    @EmbeddedId
    private HechoRefKey key;

    public HechoRef(Long id, Fuente fuente) {
        this.key = new HechoRefKey(id,fuente);
    }

    public HechoRef() {

    }
}
