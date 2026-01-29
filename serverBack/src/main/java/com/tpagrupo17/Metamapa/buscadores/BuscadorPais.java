package com.tpagrupo17.Metamapa.buscadores;

import com.tpagrupo17.Metamapa.entities.DbMain.Pais;
import com.tpagrupo17.Metamapa.repositories.DbMain.IPaisRepository;
import com.tpagrupo17.Metamapa.repositories.DbMain.IProvinciaRepository;
import com.tpagrupo17.Metamapa.utils.Geocodificador;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BuscadorPais {

    private final IPaisRepository repoPais;
    private final IProvinciaRepository repoProvincia;

    public BuscadorPais(IPaisRepository repoPais, IProvinciaRepository repoProvincia) {
        this.repoPais = repoPais;
        this.repoProvincia = repoProvincia;
    }

    public Pais buscar(String elemento) {
        return elemento != null ? this.repoPais.findByNombreNormalizado(elemento).orElse(null) : null;
    }

    public Pais buscar(Long id){
        return id != null ? this.repoPais.findById(id).orElse(null) : null;
    }

    // Corre cuando la app está lista (después de los context refresh); lo ordenamos bien abajo
    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE) // último, por las dudas
    @Transactional(transactionManager = "mainTransactionManager") // <- CLAVE en multi-DB
    public void init() {

        if (repoPais.count() > 0) return;

        var paisesProvincias = Geocodificador.obtenerTodosLosPaises();

        for (var pp : paisesProvincias) {
            repoPais.save(pp.getPais());
            repoProvincia.saveAll(pp.getProvincias());
        }
    }
}
