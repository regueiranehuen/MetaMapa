package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.HechoRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IHechoRefRepository extends JpaRepository<HechoRef,Long> {
    @Query(value = """
        select hr from HechoRef hr
        where hr.key.fuente = :fuente
""")
    List<HechoRef> findByFuente(@Param("fuente") String fuente);
}
