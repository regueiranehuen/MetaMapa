package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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