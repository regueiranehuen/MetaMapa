package com.tpagrupo17.Metamapa.entities.DbMain.filtros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;
import com.tpagrupo17.Metamapa.buscadores.Normalizador;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "filtro_descripcion")
@Entity
public class FiltroDescripcion extends Filtro {
    @Column(name = "descripcion", length = 50)
    String descripcion;

    public FiltroDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public FiltroDescripcion() {

    }


    @Override
    public Boolean aprobarHecho(Hecho hecho){
        List<String> palabrasHecho = Normalizador.normalizarSeparado(hecho.getAtributosHecho().getDescripcion());
        List<String> palabrasFiltro = Normalizador.normalizarSeparado(this.descripcion);

        // Si la descripcion del hecho enviado por parametro tiene todas sus palabras contenidas en el filtro de la descripcion
        return palabrasHecho.containsAll(palabrasFiltro);
    }

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return (root, query, cb) -> {
            if (this.descripcion == null || this.descripcion.isBlank()) return cb.conjunction();

            // dividimos en palabras, quitamos espacios extras
            List<String> palabras = Normalizador.normalizarSeparado(this.descripcion);

            List<Predicate> ands = new ArrayList<>();
            for (String palabra : palabras) {
                ands.add(cb.like(root.get("atributosHecho").get("descripcion"), "%" + palabra + "%"));
            }

            return ands.isEmpty() ? cb.conjunction() : cb.and(ands.toArray(new Predicate[0]));
        };
    }


}


