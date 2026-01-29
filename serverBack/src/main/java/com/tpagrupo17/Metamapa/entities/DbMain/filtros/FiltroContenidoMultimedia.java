package com.tpagrupo17.Metamapa.entities.DbMain.filtros;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Join;
import lombok.Getter;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;
import com.tpagrupo17.Metamapa.entities.atributosHecho.TipoContenido;
import org.springframework.data.jpa.domain.Specification;

@Getter

@Table(name = "filtro_contenido_multimedia")
@Entity
public class FiltroContenidoMultimedia extends Filtro {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoContenidoMultimedia", nullable = false, length = 20)
    TipoContenido tipoContenido;

    public FiltroContenidoMultimedia(TipoContenido tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public FiltroContenidoMultimedia() {

    }

    @Override
    public Boolean aprobarHecho(Hecho hecho) {
        return hecho.getAtributosHecho().getContenidosMultimedia().stream().anyMatch(contenidoMultimedia -> contenidoMultimedia.getTipo().equals(this.tipoContenido));
    }

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return (root, query, cb) -> {
            // join a la lista de contenidos
            Join<Object, Object> joinContenidos = root
                    .join("atributosHecho")
                    .join("contenidosMultimedia");
            // condicion: tipo == this.tipoContenido
            return cb.equal(joinContenidos.get("tipo"), this.tipoContenido);
        };
    }

}
