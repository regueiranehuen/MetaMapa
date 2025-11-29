package modulos.buscadores;

import modulos.agregacion.entities.DbMain.Provincia;
import modulos.agregacion.repositories.DbMain.IProvinciaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuscadorProvincia {

    private final IProvinciaRepository repoProvincia;

    public BuscadorProvincia(IProvinciaRepository repoProvincia) {
        this.repoProvincia = repoProvincia;
    }

    public Provincia buscarConPais(String elemento, Long id_pais) {
        if (elemento!=null)
            return this.repoProvincia.findByNombreNormalizadoAndPaisId(elemento, id_pais).orElse(null);
        return null;
    }

    public Provincia buscar(String elemento) {
        if (elemento!=null)
            return this.repoProvincia.findByNombreNormalizado(elemento).orElse(null);
        return null;
    }


    public Provincia buscar(Long elemento) {
        return elemento != null ? repoProvincia.findById(elemento).orElse(null) : null;
    }

    public List<Provincia> buscarTodos() {
        System.out.println("ENTRO EN BUSCAR TODOS");

        return this.repoProvincia.findAll();
    }

}
