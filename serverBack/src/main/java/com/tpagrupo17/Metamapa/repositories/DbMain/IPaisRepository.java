package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IPaisRepository extends JpaRepository<Pais, Long> {
    @Query("""
SELECT p
FROM Pais p
WHERE
  REPLACE(LOWER(p.pais), ' ', '') =
  REPLACE(LOWER(:nombre), ' ', '')
  OR EXISTS (
    SELECT 1
    FROM Sinonimo s
    WHERE s MEMBER OF p.sinonimos
      AND REPLACE(LOWER(s.sinonimoStr), ' ', '') =
          REPLACE(LOWER(:nombre), ' ', '')
  )
""")
    Optional<Pais> findByNombreNormalizado(@Param("nombre") String nombre);
}
