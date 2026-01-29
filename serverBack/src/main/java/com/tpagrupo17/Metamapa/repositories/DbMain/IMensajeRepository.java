package com.tpagrupo17.Metamapa.repositories.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.Mensaje;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByReceptor(Usuario receptor);
}
