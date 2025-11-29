package modulos.agregacion.entities.DbMain.filtros;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbMain.Hecho;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Setter
@Getter
@Table(name = "filtro_fecha_carga")
@Entity
public class FiltroFechaCarga extends Filtro {
    @Column(name = "fecha_inicial", length = 50)
    private LocalDateTime fechaInicial;

    @Column(name = "fecha_final", length = 50)
    private LocalDateTime fechaFinal;

    public FiltroFechaCarga(LocalDateTime fechaInicial, LocalDateTime fechaFinal){
        this.fechaInicial = fechaInicial;
        this.fechaFinal = fechaFinal;
    }

    public FiltroFechaCarga() {

    }

    @Override
    public Boolean aprobarHecho(Hecho hecho) {
        LocalDateTime fechaCarga = hecho.getAtributosHecho().getFechaCarga();

        return !fechaCarga.isBefore(fechaInicial) && !fechaCarga.isAfter(fechaFinal);
    }


    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return ((root, query, criteriaBuilder) -> {
            Path<LocalDateTime> pathFecha = root.get("atributosHecho").get("fechaCarga");
            Predicate predicado1 = criteriaBuilder.greaterThan(pathFecha, this.fechaInicial);
            Predicate predicado2 = criteriaBuilder.lessThan(pathFecha, this.fechaFinal);
            return criteriaBuilder.and(predicado1,predicado2);
        });
    }

}
