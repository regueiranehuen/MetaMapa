package com.tpagrupo17.Metamapa.repositories.DbDinamica;

import com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes.SolicitudHecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ISolicitudEliminarHechoRepository extends JpaRepository<SolicitudHecho, Long> {

    @Query(value = """
    SELECT COUNT(*) as total_spam
    FROM solicitud_hecho AS s
    WHERE s.rechazadaPorSpam = 1""",nativeQuery = true)
    Long obtenerCantSolicitudesEliminacionSpam();
}