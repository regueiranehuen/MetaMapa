package modulos.agregacion.repositories.DbProxy;

import modulos.agregacion.entities.DbDinamica.HechoDinamica;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.agregacion.entities.DbMain.projections.CategoriaCantidadProjection;
import modulos.agregacion.entities.DbMain.projections.HoraCategoriaProjection;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IHechosProxyRepository extends JpaRepository<HechoProxy, Long>, JpaSpecificationExecutor<HechoProxy> {
    @Query("""
SELECT h
FROM HechoProxy h 
WHERE REPLACE(LOWER(h.atributosHecho.titulo), ' ', '') =
      REPLACE(LOWER(:nombre), ' ', '')
""")
    List<HechoProxy> findAllByNombreNormalizado(@Param("nombre") String nombre);

    @Query("""
SELECT h
FROM HechoProxy h 
WHERE REPLACE(LOWER(h.atributosHecho.titulo), ' ', '') =
      REPLACE(LOWER(:nombre), ' ', '')
""")
    Optional<HechoProxy> findByNombreNormalizado(@Param("nombre") String nombre);


    // ¿Cuál es la categoría con mayor cantidad de hechos reportados?
    @Query(value = """
        select categoria_id as categoriaId, count(h.id) as cantHechos
        from hecho_proxy h
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
FROM hecho_proxy h
GROUP BY horaDelDia, h.categoria_id
ORDER BY totalHechos DESC
""", nativeQuery = true)
    Optional<List<HoraCategoriaProjection>> horaMayorCantHechos();

    @Query(value = """
        select h.atributosHecho.ubicacion_id from HechoProxy h where h.id = :hecho_id
""")
    Long findUbicacionIdByHechoId(@Param("hecho_id") Long hecho_id);


    @Query(value = """
        select h from HechoProxy h where h.atributosHecho.categoria_id = :categoria_id
""")
    List<HechoProxy> findAllByCategoriaId(@Param("categoria_id") Long categoria_id);

    @Query(value = """
        select h from HechoProxy h where h.activo = true
""")
    List<HechoProxy> findAllByActivoTrue();

    @Query(value = """
        select h from HechoProxy h where h.activo = true and h.atributosHecho.latitud is not null and h.atributosHecho.longitud is not null
""")
    List<HechoProxy> findAllByActivoTrueAndLatitudYLongitudNotNull();

    @Query(value = """
    SELECT COUNT(h) FROM HechoProxy h WHERE h.activo = true
    """)
    Long getCantHechos();

    @Query(value = """
    SELECT h FROM HechoProxy h
    WHERE h.activo = true order by h.cant_accesos DESC
    LIMIT 3
    """)
    List<HechoProxy> findHechosDestacados();
}