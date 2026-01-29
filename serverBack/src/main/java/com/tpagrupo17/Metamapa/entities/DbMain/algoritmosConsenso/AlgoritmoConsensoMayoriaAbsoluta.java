package com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso;

import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;
import com.tpagrupo17.Metamapa.entities.DbMain.Fuente;
import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.HechoRef;
import com.tpagrupo17.Metamapa.buscadores.BuscadorHecho;

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
            
            
            if (datasets.size() == buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId())){
                nuevosHechosConsensuados.add(hechoRef);
            }
        }
        coleccion.setHechosConsensuados(nuevosHechosConsensuados);
    }
}
