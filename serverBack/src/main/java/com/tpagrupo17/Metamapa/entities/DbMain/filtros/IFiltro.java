package com.tpagrupo17.Metamapa.entities.DbMain.filtros;

import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;
import org.springframework.data.jpa.domain.Specification;

public interface IFiltro {
    Boolean aprobarHecho(Hecho hecho);
    <T> Specification<T> toSpecification(Class<T> clazz);
}

