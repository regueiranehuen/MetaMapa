package com.tpagrupo17.Metamapa.servicioEstadistica.repositories;

import com.tpagrupo17.Metamapa.servicioEstadistica.entities.CategoriaCantidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoriaCantidadRepository extends JpaRepository<CategoriaCantidad, Long> {
}
