package modulos.agregacion.entities.DbMain.algoritmosConsenso;

import modulos.agregacion.entities.DbMain.Coleccion;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.DbMain.Fuente;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.buscadores.BuscadorHecho;

import java.util.ArrayList;
import java.util.List;

public class AlgoritmoConsensoMayoriaAbsoluta implements IAlgoritmoConsenso {

    // Absoluta: si todas las fuentes contienen el mismo hecho, se lo considera consensuado.
    // Si todas las fuentes tienen al hecho -> el size de la lista de datasets es igual al size de los datasets de all el sistema
    @Override
    public void ejecutarAlgoritmoConsenso(BuscadorHecho buscadorHecho, List<Dataset> datasets, Coleccion coleccion) {
        // primero busco los ids de los hechos ref estaticos, pq son los unicos que pueden ser consensuados
        List<HechoRef> hechosRefEstaticos = coleccion.getHechos().stream().
                filter(h->h.getKey().getFuente().equals(Fuente.ESTATICA)).
                toList();

        List<HechoRef> nuevosHechosConsensuados = new ArrayList<>();
        for (HechoRef hechoRef: hechosRefEstaticos){
            System.out.println("DATASET SIZE: " + datasets.size());
            System.out.println("CANT DATASETS HECHO: " + buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId()));
            if (datasets.size() == buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId())){
                nuevosHechosConsensuados.add(hechoRef);
            }
        }
        coleccion.setHechosConsensuados(nuevosHechosConsensuados);
    }
}
