package com.tpagrupo17.Metamapa.servicioEstadistica.repositories;

import com.tpagrupo17.Metamapa.servicioEstadistica.entities.Estadisticas;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEstadisticasRepository extends JpaRepository<Estadisticas, Long> {
}
