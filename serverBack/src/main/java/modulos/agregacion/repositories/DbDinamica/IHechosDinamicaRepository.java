package modulos.agregacion.repositories.DbDinamica;

import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.Categoria;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.agregacion.entities.DbMain.projections.CategoriaCantidadProjection;
import modulos.agregacion.entities.DbMain.projections.HoraCategoriaProjection;
import modulos.servicioEstadistica.entities.CategoriaCantidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/*

@Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
   UPDATE Hecho h
   SET h.atributosHecho.modificado = false
   WHERE COALESCE(h.atributosHecho.modificado, false) = true
""")
    int resetAllModificado();

*/

public interface IHechosDinamicaRepository extends JpaRepository<HechoDinamica, Long>, JpaSpecificationExecutor<HechoDinamica> {

    @Query("""
    select h
    from HechoDinamica h
    where h.id = :idHecho
    and h.usuario_id = :idUsuario
""")
    Optional<HechoDinamica> findByIdAndUsuario(@Param("idHecho") Long idHecho, @Param("idUsuario") Long idUsuario);

    @Query("""
SELECT h
FROM HechoDinamica h 
WHERE REPLACE(LOWER(h.atributosHecho.titulo), ' ', '') =
      REPLACE(LOWER(:nombre), ' ', '')
""")
    List<HechoDinamica> findAllByNombreNormalizado(@Param("nombre") String nombre);

    @Query("""
SELECT h
FROM HechoDinamica h 
WHERE REPLACE(LOWER(h.atributosHecho.titulo), ' ', '') =
      REPLACE(LOWER(:nombre), ' ', '')
""")
    Optional<HechoDinamica> findByNombreNormalizado(@Param("nombre") String nombre);

    // ¿Cuál es la categoría con mayor cantidad de hechos reportados?
    @Query(value = """
        select categoria_id as categoriaId, count(h.id) as cantHechos
        from hecho_dinamica h
        group by categoria_id
        order by cantHechos desc
""", nativeQuery = true)
    Optional<List<CategoriaCantidadProjection>> categoriaMayorCantHechos();

    @Query(value = """
SELECT
  IF(
    h.fechaAcontecimiento IS NULL,
    NULL,
    HOUR(
      STR_TO_DATE(
        REPLACE(SUBSTRING(h.fechaAcontecimiento,1,19),'T',' '),
        '%Y-%m-%d %H:%i:%s'
      )
    )
  ) AS horaDelDia,
  COUNT(h.id) AS totalHechos,
  h.categoria_id        AS idCategoria
FROM hecho_dinamica h
GROUP BY horaDelDia, h.categoria_id
ORDER BY totalHechos DESC
LIMIT 1;
""", nativeQuery = true)
    Optional<List<HoraCategoriaProjection>> horaMayorCantHechos();


    @Query(value = """
        select h.atributosHecho.ubicacion_id from HechoDinamica h where h.id = :hecho_id
""")
    Long findUbicacionIdByHechoId(@Param("hecho_id") Long hecho_id);


    @Query(value = """
        select h from HechoDinamica h where h.atributosHecho.categoria_id = :categoria_id
""")
    List<HechoDinamica> findAllByCategoriaId(@Param("categoria_id") Long categoria_id);

    @Query(value = """
        select h from HechoDinamica h where h.activo = true
""")
    List<HechoDinamica> findAllByActivoTrue();


    @Query(value = """
        select h from HechoDinamica h where h.activo = true and h.atributosHecho.latitud is not null and h.atributosHecho.longitud is not null
""")
    List<HechoDinamica> findAllByActivoTrueAndLatitudYLongitudNotNull();

    @Query(value = """
    SELECT COUNT(h) FROM HechoDinamica h WHERE h.activo = true
    """)
    Long getCantHechos();

    @Query("""
        SELECT h
        FROM HechoDinamica h
        WHERE h.usuario_id = :usuarioId AND h.activo = true
    """)
    List<HechoDinamica> findAllByUsuarioIdAndActivoTrue(@Param("usuarioId") Long usuarioId);

    @Query(value = """
    SELECT h FROM HechoDinamica h
    WHERE h.activo = true order by h.cant_accesos DESC
    LIMIT 3
    """)
    List<HechoDinamica> findHechosDestacados();
}