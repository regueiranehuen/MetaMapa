package com.tpagrupo17.Metamapa.repositories.DbDinamica;

import com.tpagrupo17.Metamapa.entities.DbDinamica.Solicitudes.SolicitudHecho;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISolicitudModificarHechoRepository extends JpaRepository<SolicitudHecho, Long> {
}