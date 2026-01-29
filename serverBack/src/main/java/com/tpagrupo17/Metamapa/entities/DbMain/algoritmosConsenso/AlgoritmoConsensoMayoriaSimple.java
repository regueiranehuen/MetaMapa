package com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso;

import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;
import com.tpagrupo17.Metamapa.entities.DbMain.Fuente;
import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.HechoRef;
import com.tpagrupo17.Metamapa.buscadores.BuscadorHecho;

import java.util.ArrayList;
import java.util.List;

public class AlgoritmoConsensoMayoriaSimple implements IAlgoritmoConsenso {

    // Mayoría simple: si al menos la mitad de las fuentes contienen el mismo hecho, se lo considera consensuado
    @Override
    public void ejecutarAlgoritmoConsenso(BuscadorHecho buscadorHecho, List<Dataset> datasets, Coleccion coleccion) {
        // primero busco los ids de los hechos ref estaticos, pq son los unicos que pueden ser consensuados
        List<HechoRef> hechosRefEstaticos = coleccion.getHechos().stream().
                filter(h->h.getKey().getFuente().equals(Fuente.ESTATICA)).
                toList();

        List<HechoRef> nuevosHechosConsensuados = new ArrayList<>();

        for (HechoRef hechoRef: hechosRefEstaticos){
            if (datasets.size()/2 <= buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId())){
                //coleccion.getHechosConsensuados().add(hechoRef);
                nuevosHechosConsensuados.add(hechoRef);
            }
        }
        coleccion.setHechosConsensuados(nuevosHechosConsensuados);
    }
}
