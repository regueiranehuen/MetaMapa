package modulos.agregacion.entities.DbMain.filtros;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.agregacion.entities.DbMain.Provincia;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Getter
@Setter
@Table(name = "filtro_provincia")
@Entity
public class FiltroProvincia extends Filtro{
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_provincia", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_filtro_provincia_provincia"))
    private Provincia provincia;

    @ElementCollection
    private List<Long> ubicaciones_ids;

    public FiltroProvincia(Provincia provincia, List<Long> ubicaciones_ids) {
        this.provincia = provincia;
        this.ubicaciones_ids = ubicaciones_ids;
    }

    public void refrescarUbicaciones_ids(List<Long> ubicaciones_ids){
        this.ubicaciones_ids = ubicaciones_ids;
    }

    public FiltroProvincia() {

    }

    /*@Override
    public Boolean aprobarHecho(Hecho hecho){
        return hecho.getAtributosHecho().getUbicacion().getProvincia().getId().equals(this.provincia.getId());
    }*/

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {

        System.out.println("HOLA!! ENTRE A PROVINCIA");

        if (ubicaciones_ids == null || ubicaciones_ids.isEmpty()) {
            System.out.println("Lista de ubicaciones vacía -> Filtro FALSE");
            return (root, query, cb) -> cb.disjunction(); // Nunca se cumple
        }

        for (Long id : ubicaciones_ids) {
            System.out.println("SOY UNA UBICACION EN PROVINCIA: " + id);
        }

        return (root, query, cb) -> {
            Path<Long> pathUbicacionId = root
                    .get("atributosHecho")
                    .get("ubicacion_id");

            CriteriaBuilder.In<Long> inClause = cb.in(pathUbicacionId);
            ubicaciones_ids.forEach(inClause::value);

            return inClause;
        };
    }



}
