package modulos.agregacion.entities.fuentes;

import lombok.Getter;
import lombok.Setter;
import modulos.agregacion.entities.DbEstatica.Dataset;
import modulos.agregacion.entities.DbEstatica.HechoEstatica;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import modulos.buscadores.*;

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
        else if (formato.equals("json")){
            //TODO el lector del formato JSON
            List<HechoEstatica> lista = new ArrayList<>();
            return lista;
        }

        List<HechoEstatica> lista = new ArrayList<>();
        return lista;
    }
}
