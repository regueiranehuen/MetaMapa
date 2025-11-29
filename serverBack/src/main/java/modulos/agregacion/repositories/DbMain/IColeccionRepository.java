package modulos.agregacion.repositories.DbMain;

import modulos.agregacion.entities.DbMain.Coleccion;
import modulos.agregacion.entities.DbMain.projections.ColeccionProvinciaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface IColeccionRepository extends JpaRepository<Coleccion, Long> {

    @Query("""
    SELECT c FROM Coleccion c where c.activo = true
    """)
    List<Coleccion> findAllByActivoTrue();

    @Query("""
    SELECT c FROM Coleccion c where c.activo = true and c.modificado = true
    """)
    List<Coleccion> findAllByActivoTrueAndModificadoTrue();

    @Query("""
    SELECT c FROM Coleccion c where c.id = :id_coleccion AND c.activo = true
    """)
    Optional<Coleccion> findByIdAndActivoTrue(@Param("id_coleccion") Long idColeccion);

    @Query(value = """
    SELECT COUNT(c) FROM Coleccion c WHERE c.activo = true
    """)
    Long cantColecciones();

    @Query(value = """
    SELECT c FROM Coleccion c
    WHERE c.activo = true order by c.cant_accesos DESC
    LIMIT 3
    """)
    List<Coleccion> findColeccionesDestacadas();
}