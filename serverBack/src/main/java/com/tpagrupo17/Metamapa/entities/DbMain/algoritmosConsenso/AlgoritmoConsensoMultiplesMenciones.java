package com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso;

import com.tpagrupo17.Metamapa.entities.DbMain.Fuente;
import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.HechoRef;
import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;
import com.tpagrupo17.Metamapa.buscadores.BuscadorHecho;

import java.util.ArrayList;
import java.util.List;

public class AlgoritmoConsensoMultiplesMenciones implements IAlgoritmoConsenso {

    /* múltiples menciones: si al menos dos fuentes contienen un mismo hecho y ninguna
       otra fuente contiene otro de igual título pero diferentes atributos, se lo considera consensuado */

    // Entiendo que basta con tener un solo atributo distinto como condicion suficiente
    @Override
    public void ejecutarAlgoritmoConsenso(BuscadorHecho buscadorHecho, List<Dataset> datasets, Coleccion coleccion) {
        // primero busco los ids de los hechos ref estaticos, pq son los unicos que pueden ser consensuados

        

        List<HechoRef> hechosRefEstaticos = coleccion.getHechos().stream().
                filter(h->h.getKey().getFuente().equals(Fuente.ESTATICA)).
                toList();

        List<HechoRef> nuevosHechosConsensuados = new ArrayList<>();

        for (HechoRef hechoRef: hechosRefEstaticos) {
            
            
            if (buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId()) >= 2 &&
            buscadorHecho.findCantHechosIgualTituloDiferentesAtributos(hechoRef.getKey().getId()) == 0) {
                
                //coleccion.getHechosConsensuados().add(hechoRef);
                nuevosHechosConsensuados.add(hechoRef);
            }
        }
        coleccion.setHechosConsensuados(nuevosHechosConsensuados);
    }
}