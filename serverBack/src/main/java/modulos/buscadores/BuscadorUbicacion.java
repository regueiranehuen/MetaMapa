package modulos.buscadores;

import modulos.agregacion.entities.DbMain.Pais;
import modulos.agregacion.entities.DbMain.Provincia;
import modulos.agregacion.entities.DbMain.Ubicacion;
import modulos.agregacion.repositories.DbMain.IUbicacionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BuscadorUbicacion {

    private final IUbicacionRepository repoUbicacion;

    public BuscadorUbicacion(IUbicacionRepository repoUbicacion) {
        this.repoUbicacion = repoUbicacion;
    }

    public Ubicacion buscar(Long id_pais, Long id_provincia) {

        return this.repoUbicacion.findByPaisIdAndProvinciaId(id_pais,id_provincia).orElse(null);
    }

    public Ubicacion buscarUbicacion(Long idUbicacion) {
        if (idUbicacion == null)
            return null;
        return repoUbicacion.findById(idUbicacion).orElse(null);
    }

    public List<Long> buscarUbicacionesConPais(Long pais_id){
        return repoUbicacion.findAllUbicacionesIdByPaisId(pais_id);
    }

    public List<Long> buscarUbicacionesConProvincia(Long provincia_id){
        return repoUbicacion.findAllUbicacionesIdByProvinciaId(provincia_id);
    }

    @Transactional
    public Ubicacion buscarOCrear(Pais pais, Provincia provincia){

        Long id_pais;
        Long id_provincia;

        if(pais == null){
            id_pais = null;
        }
        else{
            id_pais = pais.getId();
        }
        if(provincia == null){
            id_provincia = null;
        }
        else {
            id_provincia = provincia.getId();
        }

        Ubicacion ubicacion = this.buscar(id_pais, id_provincia);

        if (ubicacion == null){
            if (pais != null && provincia != null){
                if (provincia.getPais().getId().equals(pais.getId())){
                    ubicacion = new Ubicacion(pais, provincia);
                    repoUbicacion.save(ubicacion);
                }
            }
            else{
                if(pais != null || provincia != null) {
                    ubicacion = new Ubicacion(pais, provincia);
                    repoUbicacion.save(ubicacion);
                }
            }
        }

        return ubicacion;
    }
}
