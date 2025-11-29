package modulos.agregacion.repositories.DbMain;

import org.springframework.data.jpa.repository.JpaRepository;
import modulos.agregacion.entities.DbMain.Sinonimo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ISinonimoRepository extends JpaRepository<Sinonimo, Long> {

    @Query("""
    SELECT s FROM Categoria c 
    JOIN Sinonimo s ON c.id = :id_categoria AND s.sinonimoStr = :nombre
    """)
    Optional<Sinonimo> findByIdCategoriaAndNombre(@Param("id_categoria") Long id_categoria, @Param("nombre") String nombre);


    @Query("""
    SELECT s FROM Pais p 
    JOIN Sinonimo s ON p.id = :id_pais AND s.sinonimoStr = :nombre
    """)
    Optional<Sinonimo> findByIdPaisAndNombre(@Param("id_pais") Long idPais, @Param("nombre") String sinonimoStr);

    @Query("""
    SELECT s FROM Provincia p 
    JOIN Sinonimo s ON p.id = :id_provincia AND s.sinonimoStr = :nombre
    """)
    Optional<Sinonimo> findByIdProvinciaAndNombre(@Param("id_provincia") Long idPais, @Param("nombre") String sinonimoStr);


}
