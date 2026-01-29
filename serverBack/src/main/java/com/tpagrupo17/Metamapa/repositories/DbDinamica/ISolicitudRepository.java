package com.tpagrupo17.Metamapa.repositories.DbDinamica;

import com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes.SolicitudHecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ISolicitudRepository extends JpaRepository<SolicitudHecho, Long> {
    @Query(value = """
    SELECT s
    FROM SolicitudHecho s
    WHERE s.procesada = false
""")
    List<SolicitudHecho> obtenerSolicitudesPendientes();

    @Query("""
    SELECT s FROM SolicitudHecho s WHERE s.procesada = false AND s.id = :id_solicitud
    """)
    Optional<SolicitudHecho> findByIdAndProcesadaFalse(@Param("id_solicitud") Long idSolicitud);

    @Query(value = """
            SELECT ifnull((COUNT(*) * 100.0 / (SELECT COUNT(*) FROM solicitud_hecho)) ,0)
                FROM solicitud_hecho
                WHERE procesada = true
    """, nativeQuery = true)
    Double porcentajeProcesadas();

    @Query(value = """
    SELECT s FROM SolicitudHecho s WHERE s.procesada = false
    """)
    List<SolicitudHecho> findAllByProcesadaFalse();
}
