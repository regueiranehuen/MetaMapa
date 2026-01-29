package com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso;

import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;
import com.tpagrupo17.Metamapa.buscadores.BuscadorHecho;

import java.util.List;

public interface IAlgoritmoConsenso {
    void ejecutarAlgoritmoConsenso(BuscadorHecho buscadorHecho, List<Dataset> datasets, Coleccion coleccion);
}
