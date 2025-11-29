package modulos.agregacion.entities.DbMain.filtros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.buscadores.Normalizador;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "filtro_fecha_acontecimiento")
public class FiltroFechaAcontecimiento extends Filtro {

    @Column(name = "fechaInicial", length = 50)
    private LocalDateTime fechaInicial;

    @Column(name = "fechaFinal", length = 50)
    private LocalDateTime fechaFinal;

    public FiltroFechaAcontecimiento(LocalDateTime fechaInicial, LocalDateTime fechaFinal){
        this.fechaInicial = fechaInicial;
        this.fechaFinal = fechaFinal;
    }

    public FiltroFechaAcontecimiento() {

    }

    /// Paso entre dos fechas?
    @Override
    public Boolean aprobarHecho(Hecho hecho){

        LocalDateTime fechaHecho = hecho.getAtributosHecho().getFechaAcontecimiento();

        return !fechaHecho.isAfter(fechaInicial) && !fechaHecho.isBefore(fechaFinal);
    }

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return ((root, query, criteriaBuilder) -> {
            Path<LocalDateTime> pathFecha = root.get("atributosHecho").get("fechaAcontecimiento");
            Predicate predicado1 = criteriaBuilder.greaterThan(pathFecha, this.fechaInicial);
            Predicate predicado2 = criteriaBuilder.lessThan(pathFecha, this.fechaFinal);
            return criteriaBuilder.and(predicado1,predicado2);
        });
    }

}
