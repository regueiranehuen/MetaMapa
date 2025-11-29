package modulos.agregacion.repositories.DbEstatica;

import modulos.agregacion.entities.DbEstatica.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IDatasetsRepository extends JpaRepository<Dataset, Long> {

    @Query("""
    SELECT d FROM Dataset d where d.fuente = :fuente
    """)
    Optional<Dataset> findByFuente(@Param("fuente") String fuente);

    @Query("""
    SELECT DISTINCT d.fuente
    FROM HechoEstatica h
    JOIN h.datasets d
    WHERE h.id IN :hechoIds
    """)
    List<String> findDistinctDatasetsByHechoIds(@Param("hechoIds") List<Long> hechoIds);

    @Query("""
    SELECT COUNT(d) FROM Dataset d
    """)
    Integer getCantFuentes();
}