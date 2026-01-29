package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUbicacionRepository extends JpaRepository<Ubicacion, Long> {
    @Query(value = """
    SELECT *
    FROM ubicacion u
    WHERE 
        ((:paisId IS NULL AND u.pais_id IS NULL) OR (:paisId IS NOT NULL AND u.pais_id = :paisId))
      AND
        ((:provinciaId IS NULL AND u.provincia_id IS NULL) OR (:provinciaId IS NOT NULL AND u.provincia_id = :provinciaId))
    LIMIT 1
""", nativeQuery = true) // El limit 1 porque sin querer ya guardé ubicaciones con fks repetidas
    Optional<Ubicacion> findByPaisIdAndProvinciaId(@Param("paisId") Long paisId,
                                                   @Param("provinciaId") Long provinciaId);

    @Query(value = """
        select u.id from Ubicacion u where u.pais.id = :pais_id
""")
    List<Long> findAllUbicacionesIdByPaisId(@Param("pais_id") Long pais_id);


    @Query(value = """
    SELECT u.id FROM Ubicacion u where u.provincia.id = :provincia_id
    """)
    List<Long> findAllUbicacionesIdByProvinciaId(@Param ("provincia_id") Long provinciaId);
}
