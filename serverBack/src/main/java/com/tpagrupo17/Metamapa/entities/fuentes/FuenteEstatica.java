package com.tpagrupo17.Metamapa.entities.fuentes;

import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;
import com.tpagrupo17.Metamapa.entities.DbEstatica.HechoEstatica;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Usuario;
import com.tpagrupo17.Metamapa.buscadores.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FuenteEstatica {
    private Dataset dataSet;
    public List<HechoEstatica> leerFuente(Usuario usuario, BuscadoresRegistry buscadores){

        String[] nombreArchivo = this.dataSet.getStoragePath().split("\\.");
        // nombreArchivo[0] = nombre archivo
        // nombreArchivo[1] = tipo de archivo
        String formato = nombreArchivo[1].toLowerCase();
        if (formato.equals("csv")){
            var lectorCSV = new LectorCSV(this.dataSet);
            return lectorCSV.leerCSV(usuario, buscadores);
        }
        List<HechoEstatica> lista = new ArrayList<>();
        return lista;
    }
}
