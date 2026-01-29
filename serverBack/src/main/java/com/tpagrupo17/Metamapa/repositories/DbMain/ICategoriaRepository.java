package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {


    @Query("""
SELECT c
FROM Categoria c
WHERE
  REPLACE(LOWER(c.titulo), ' ', '') =
  REPLACE(LOWER(:nombre), ' ', '')
  OR EXISTS (
    SELECT 1
    FROM Sinonimo s
    WHERE s MEMBER OF c.sinonimos
      AND REPLACE(LOWER(s.sinonimoStr), ' ', '') =
          REPLACE(LOWER(:nombre), ' ', '')
  )
""")
    Optional<Categoria> findByNombreNormalizado(@Param("nombre") String nombre);

}


