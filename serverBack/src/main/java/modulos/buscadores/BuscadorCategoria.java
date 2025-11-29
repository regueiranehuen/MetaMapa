package modulos.buscadores;

import modulos.agregacion.entities.DbMain.Categoria;
import modulos.agregacion.repositories.DbMain.ICategoriaRepository;
import org.springframework.stereotype.Component;

@Component
public class BuscadorCategoria {

    private final ICategoriaRepository categoriaRepository;

    public BuscadorCategoria(ICategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria buscar(String elemento) {
        if (elemento != null)
            return this.categoriaRepository.findByNombreNormalizado(elemento).orElse(null);
        return null;
    }

    public Categoria buscar(Long id){
        if (id == null)
            return null;
        return this.categoriaRepository.findById(id).orElse(null);
    }



}