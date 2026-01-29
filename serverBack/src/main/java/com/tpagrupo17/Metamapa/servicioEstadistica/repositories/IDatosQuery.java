package com.tpagrupo17.Metamapa.servicioEstadistica.repositories;

import com.tpagrupo17.Metamapa.entities.DbMain.Categoria;
import com.tpagrupo17.Metamapa.entities.DbMain.Coleccion;
import com.tpagrupo17.Metamapa.entities.DbMain.Provincia;
import com.tpagrupo17.Metamapa.servicioEstadistica.entities.CategoriaCantidad;
import com.tpagrupo17.Metamapa.servicioEstadistica.entities.CategoriaHora;
import com.tpagrupo17.Metamapa.servicioEstadistica.entities.CategoriaProvincia;
import com.tpagrupo17.Metamapa.servicioEstadistica.entities.ColeccionProvincia;

import java.util.List;

public interface IDatosQuery {
    //De una colección, ¿en qué provincia se agrupan la mayor cantidad de hechos reportados?
    List<ColeccionProvincia> obtenerMayorCantHechosProvinciaEnColeccion();
    //¿Cuál es la categoría con mayor cantidad de hechos reportados?
    CategoriaCantidad categoriaMayorCantHechos();
    // ¿En qué provincia se presenta la mayor cantidad de hechos de una cierta categoría?
    List<CategoriaProvincia> mayorCantHechosCategoriaXProvincia();
    //¿A qué hora del día ocurren la mayor cantidad de hechos de una cierta categoría?
    List<CategoriaHora> horaMayorCantHechos();
    //¿Cuántas solicitudes de eliminación son spam?
    Long cantSolicitudesEliminacionSpam();
    Categoria findCategoriaById(Long categoria_id);
    Provincia findProvinciaById(Long id);
    Coleccion findColeccionById(Long id);
}

