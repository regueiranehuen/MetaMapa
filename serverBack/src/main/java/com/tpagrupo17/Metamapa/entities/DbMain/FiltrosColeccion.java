package com.tpagrupo17.Metamapa.entities.DbMain;

import com.tpagrupo17.Metamapa.entities.DbMain.filtros.*;
import lombok.Data;
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
