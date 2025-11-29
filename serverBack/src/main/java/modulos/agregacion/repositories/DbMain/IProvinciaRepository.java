package modulos.agregacion.repositories.DbMain;

import modulos.agregacion.entities.DbMain.Provincia;
import modulos.agregacion.entities.DbMain.projections.CategoriaProvinciaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IProvinciaRepository extends JpaRepository<Provincia, Long> {
    // ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?
    @Query(value = """
        SELECT p.id AS provinciaId, h.categoria_id AS categoriaId, COUNT(h.id) as cantHechos
        FROM hecho h
        JOIN ubicacion u ON h.ubicacion_id = u.id
        JOIN provincia p ON u.provincia_id = p.id
        GROUP BY p.id, h.categoria_id
        ORDER BY count(h.id) DESC
        limit 1""",nativeQuery = true)
    List<CategoriaProvinciaProjection> obtenerCategoriaMayorHechosProvincia();

    // TODO: SINONIMOS DE PROVINCIAS QUE SE CORRESPONDAN CON EL PAIS ASOCIADO
    @Query("""
SELECT p
FROM Provincia p
WHERE
    p.pais.id = :pais_id AND
    (
  REPLACE(LOWER(p.provincia), ' ', '') =
  REPLACE(LOWER(:nombre), ' ', '')
  OR EXISTS (
    SELECT 1
    FROM Sinonimo s
    WHERE s MEMBER OF p.sinonimos
      AND REPLACE(LOWER(s.sinonimoStr), ' ', '') =
          REPLACE(LOWER(:nombre), ' ', '')
  )
  )
""")
    Optional<Provincia> findByNombreNormalizadoAndPaisId(@Param("nombre") String nombre, @Param("pais_id") Long pais_id);


    @Query("""
SELECT p
FROM Provincia p
WHERE
  REPLACE(LOWER(p.provincia), ' ', '') =
  REPLACE(LOWER(:nombre), ' ', '')
  
""")
    Optional<Provincia> findByNombreNormalizado(@Param("nombre") String nombre);



    @Query("""
    select p from Provincia p where p.pais.id = :id
""")
    List<Provincia> findAllByPaisId(@Param("id") Long id);

    @Query("""
    SELECT p from Provincia p where p.id = :entidad AND p.pais.id = :pais
    """)
    Optional<Provincia> findByIdAndPaisId(@Param("pais") Long id_pais, @Param("entidad") Long id_entidad);
}


//¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?

/*¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?*/
/*
select p.id, h.categoria_id from hecho h
join ubicacion u on h.ubicacion_id = u.id
join provincia p on u.id = p.id
group by p.id, h.categoria_id
order by count(h.id)
limit 1*/