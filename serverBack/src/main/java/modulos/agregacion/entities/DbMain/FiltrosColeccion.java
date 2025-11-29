package modulos.agregacion.entities.DbMain;

import lombok.Data;
import modulos.agregacion.entities.DbMain.filtros.*;

import java.util.List;


@Data
public class FiltrosColeccion {
    private List<FiltroCategoria> filtroCategoria;
    private List<FiltroPais> filtroPais;
    private FiltroDescripcion filtroDescripcion;
    private List<FiltroContenidoMultimedia> filtroContenidoMultimedia;
    private FiltroFechaAcontecimiento filtroFechaAcontecimiento;
    private FiltroFechaCarga filtroFechaCarga;
    private List<FiltroFuente> filtroFuentes;
    private FiltroTitulo filtroTitulo;
    private List<FiltroProvincia> filtroProvincia;
}
