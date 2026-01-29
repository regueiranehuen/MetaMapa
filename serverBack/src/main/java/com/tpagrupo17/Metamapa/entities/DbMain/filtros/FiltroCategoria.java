package com.tpagrupo17.Metamapa.entities.DbMain.filtros;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Path;
import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.DbMain.Categoria;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@Entity
@Table(name = "filtro_categoria")
public class FiltroCategoria extends Filtro {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_categoria", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_filtro_categoria_categoria"))
    private Categoria categoria;

    public FiltroCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public FiltroCategoria() {

    }

    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return (root, query, cb) -> {
            Path<Long> pathId = root.get("atributosHecho").get("categoria_id");
            return cb.equal(pathId, this.categoria.getId());
        };
    }

    /*@Override
    public Boolean aprobarHecho(Hecho hecho){
        return hecho.getAtributosHecho().getCategoria().getId().equals(this.categoria.getId());
    }*/

}

/*
* Una Specification<T> es básicamente una función que recibe:

root → el root de la entidad (ej: Publicacion).

query → la consulta que se está armando.

cb → el CriteriaBuilder que te da métodos como equal, lessThan, like, etc.

Y devuelve un Predicate, o sea, la condición.
*
*
* @Override
public Specification<Publicacion> toSpecification() {
    return (root, query, cb) -> {
        Path<Long> categoriaIdPath = root.get("lote").get("categoria").get("id");
        return cb.equal(categoriaIdPath, this.categoria.getId());
    };
}
*
*
*
* */