package modulos.agregacion.entities.DbMain.filtros;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.Path;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbMain.Fuente;
import modulos.agregacion.entities.DbMain.Hecho;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@Table(name = "filtro_fuente")
@Entity
public class FiltroFuente extends Filtro {
    @Enumerated(EnumType.ORDINAL)
    private Fuente fuenteDeseada;

    public FiltroFuente(Fuente fuenteDeseada){
        this.fuenteDeseada = fuenteDeseada;
    }

    public FiltroFuente() {
    }

    @Override
    public Boolean aprobarHecho(Hecho hecho){
        return hecho.getAtributosHecho().getFuente().equals(fuenteDeseada);
    }

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {
        return((root, query, cb) -> {
            Path<Long> pathId = root.get("atributosHecho").get("fuente");
            return cb.equal(pathId,this.fuenteDeseada);
        });
    }

}
