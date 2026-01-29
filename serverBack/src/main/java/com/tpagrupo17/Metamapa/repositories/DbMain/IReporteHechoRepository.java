package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IReporteHechoRepository extends JpaRepository<Reporte, Long> {
    @Query(value = """
        select r from Reporte r where r.procesado = false
""")
    List<Reporte> findAllByProcesadoFalse();
}