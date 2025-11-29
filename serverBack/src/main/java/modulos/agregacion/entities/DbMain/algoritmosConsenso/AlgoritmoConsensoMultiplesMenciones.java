package modulos.agregacion.entities.DbMain.algoritmosConsenso;

import modulos.agregacion.entities.DbMain.Fuente;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.entities.DbMain.Coleccion;
import modulos.agregacion.entities.DbMain.Ubicacion;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.DbMain.Hecho;
import modulos.buscadores.BuscadorHecho;
import modulos.buscadores.Normalizador;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AlgoritmoConsensoMultiplesMenciones implements IAlgoritmoConsenso {

    /* múltiples menciones: si al menos dos fuentes contienen un mismo hecho y ninguna
       otra fuente contiene otro de igual título pero diferentes atributos, se lo considera consensuado */

    // Entiendo que basta con tener un solo atributo distinto como condicion suficiente
    @Override
    public void ejecutarAlgoritmoConsenso(BuscadorHecho buscadorHecho, List<Dataset> datasets, Coleccion coleccion) {
        // primero busco los ids de los hechos ref estaticos, pq son los unicos que pueden ser consensuados

        System.out.println("Ejecutando algoritmo Consenso");

        List<HechoRef> hechosRefEstaticos = coleccion.getHechos().stream().
                filter(h->h.getKey().getFuente().equals(Fuente.ESTATICA)).
                toList();

        List<HechoRef> nuevosHechosConsensuados = new ArrayList<>();

        for (HechoRef hechoRef: hechosRefEstaticos) {
            System.out.println("ID hecho: " + hechoRef.getKey().getId());
            System.out.println("Cant datasets: " + buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId()));
            if (buscadorHecho.findCantDatasetsHecho(hechoRef.getKey().getId()) >= 2 &&
            buscadorHecho.findCantHechosIgualTituloDiferentesAtributos(hechoRef.getKey().getId()) == 0) {
                System.out.println("SOY ESTE HECHO: " +  hechoRef.getKey().getId());
                //coleccion.getHechosConsensuados().add(hechoRef);
                nuevosHechosConsensuados.add(hechoRef);
            }
        }
        coleccion.setHechosConsensuados(nuevosHechosConsensuados);
    }
}