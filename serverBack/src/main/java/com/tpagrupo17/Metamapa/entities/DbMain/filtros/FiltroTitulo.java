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
@Table(name = "filtro_titulo")
@Entity
public class FiltroTitulo extends Filtro {
    @Column(name = "titulo", length = 50)
    String titulo;

    public FiltroTitulo(String titulo) {
        this.titulo = titulo;
    }

    public FiltroTitulo() {

    }

    @Override
    public Boolean aprobarHecho(Hecho hecho) {
        List<String> palabrasHecho = Normalizador.normalizarSeparado(hecho.getAtributosHecho().getTitulo());
        List<String> palabrasFiltro = Normalizador.normalizarSeparado(this.titulo);

        // Si la descripcion del hecho enviado por parametro tiene todas sus palabras contenidas en el filtro de la descripcion
        return palabrasHecho.containsAll(palabrasFiltro);
    }

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return (root, query, cb) -> {
            if (this.titulo == null || this.titulo.isBlank()) return cb.conjunction();

            // dividimos en palabras, quitamos espacios extras
            List<String> palabras = Normalizador.normalizarSeparado(this.titulo);

            List<Predicate> ands = new ArrayList<>();
            for (String palabra : palabras) {
                ands.add(cb.like(root.get("atributosHecho").get("titulo"), "%" + palabra + "%"));
            }

            return ands.isEmpty() ? cb.conjunction() : cb.and(ands.toArray(new Predicate[0]));
        };
    }

}