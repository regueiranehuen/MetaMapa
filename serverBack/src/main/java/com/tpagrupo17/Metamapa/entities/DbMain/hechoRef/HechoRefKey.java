package com.tpagrupo17.Metamapa.entities.DbMain.hechoRef;

import jakarta.persistence.*;
import lombok.Getter;
import com.tpagrupo17.Metamapa.entities.DbMain.Fuente;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class HechoRefKey implements Serializable {
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuente", length = 20)
    private Fuente fuente;

    public HechoRefKey() {}
    public HechoRefKey(Long id, Fuente fuente) {
        this.id = id;
        this.fuente = fuente;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HechoRefKey that)) return false;
        return Objects.equals(id, that.id) && fuente == that.fuente;
    }
    @Override public int hashCode() { return Objects.hash(id, fuente); }

}
