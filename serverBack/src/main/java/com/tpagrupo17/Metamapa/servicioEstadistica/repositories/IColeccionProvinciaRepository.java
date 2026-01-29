package com.tpagrupo17.Metamapa.servicioEstadistica.repositories;

import com.tpagrupo17.Metamapa.servicioEstadistica.entities.ColeccionProvincia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IColeccionProvinciaRepository extends JpaRepository<ColeccionProvincia, Long> {
}
