package modulos.agregacion.entities.DbMain.filtros;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.agregacion.entities.DbMain.Pais;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "filtro_pais")
@Entity
public class FiltroPais extends Filtro {

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_pais", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_filtro_pais_pais"))
    private Pais pais;

    @ElementCollection
    private List<Long> ubicaciones_ids;

    public FiltroPais(Pais pais, List<Long> ubicaciones_ids) {
        this.pais = pais;
        System.out.println("PAIS: " + pais.getPais());
        ubicaciones_ids.forEach(a -> System.out.println("ubicaciones_ids: " + a));
        this.ubicaciones_ids = ubicaciones_ids;
    }

    public void refrescarUbicaciones_ids(List<Long> ubicaciones_ids){
        this.ubicaciones_ids = ubicaciones_ids;
    }

    public FiltroPais() {

    }

    /*@Override
    public Boolean aprobarHecho(Hecho hecho){
        return hecho.getAtributosHecho().getUbicacion().getPais().getId().equals(this.pais.getId());
    }*/

    @Override
    public <T> Specification<T> toSpecification(Class<T> clazz) {

        if (ubicaciones_ids == null || ubicaciones_ids.isEmpty()) {
            System.out.println("Lista de ubicaciones vacía -> Filtro FALSE");
            return (root, query, cb) -> cb.disjunction(); // Nunca se cumple
        }

        for(Long id: ubicaciones_ids){
            System.out.println("SOY UNA UBICACION EN PAIS: " + id);
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
