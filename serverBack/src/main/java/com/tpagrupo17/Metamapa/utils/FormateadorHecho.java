package com.tpagrupo17.Metamapa.utils;

import com.tpagrupo17.Metamapa.entities.DbMain.*;
import com.tpagrupo17.Metamapa.entities.DbMain.filtros.*;
import com.tpagrupo17.Metamapa.entities.atributosHecho.AtributosHecho;
import com.tpagrupo17.Metamapa.entities.atributosHecho.Origen;
import com.tpagrupo17.Metamapa.entities.atributosHecho.TipoContenido;
import com.tpagrupo17.Metamapa.buscadores.*;
import com.tpagrupo17.Metamapa.dtos.input.CriteriosColeccionDTO;
import com.tpagrupo17.Metamapa.dtos.input.SolicitudHechoInputDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class FormateadorHecho {

    public static List<List<IFiltro>> agruparFiltrosPorClase(List<Filtro> criterios) {
        return criterios.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Filtro::getClass // o filtro -> filtro.getClass()
                ))
                .values()
                .stream()
                .map(list -> new ArrayList<IFiltro>(list))
                .collect(Collectors.toList());
    }

    public static AtributosHecho formatearAtributosHecho(BuscadoresRegistry buscadores, SolicitudHechoInputDTO dtoInput){

    AtributosHecho atributos = new AtributosHecho();

    Pais pais = null;
    Provincia provincia = null;

    if (dtoInput.getId_pais() != null){
        pais = buscadores.getBuscadorPais().buscar(dtoInput.getId_pais());
    }

    if (dtoInput.getId_provincia() != null){
        provincia = buscadores.getBuscadorProvincia().buscar(dtoInput.getId_provincia());
    }

    if (dtoInput.getLatitud() != null){
        atributos.setLatitud(dtoInput.getLatitud());
    }

    if (dtoInput.getLongitud() != null){
        atributos.setLongitud(dtoInput.getLongitud());
    }

    Ubicacion ubicacion = buscadores.getBuscadorUbicacion().buscarOCrear(pais,provincia);
    atributos.setUbicacion_id(ubicacion!=null ? ubicacion.getId() : null);

    if (dtoInput.getId_categoria() != null){
        Categoria categoria = buscadores.getBuscadorCategoria().buscar(dtoInput.getId_categoria());
        atributos.setCategoria_id(categoria!=null ? categoria.getId() : null);
    }

    atributos.setTitulo(dtoInput.getTitulo());

    if(dtoInput.getDescripcion() != null) {
        atributos.setDescripcion(dtoInput.getDescripcion());
    }
    if(dtoInput.getFechaAcontecimiento() != null) {
        atributos.setFechaAcontecimiento(FechaParser.parsearFecha(dtoInput.getFechaAcontecimiento()));
    }else{
        atributos.setFechaAcontecimiento(null);
    }

    atributos.setOrigen(Origen.CARGA_MANUAL);

    return atributos;

}


    public static FiltrosColeccion formatearFiltrosColeccion(
            BuscadoresRegistry buscadores,
            CriteriosColeccionDTO inputDTO) {

        FiltrosColeccion filtros = new FiltrosColeccion();

        BuscadorCategoria buscadorCategoria = buscadores.getBuscadorCategoria();
        BuscadorPais buscadorPais = buscadores.getBuscadorPais();
        BuscadorProvincia buscadorProvincia = buscadores.getBuscadorProvincia();
        BuscadorFiltro buscadorFiltro = buscadores.getBuscadorFiltro();
        BuscadorUbicacion buscadorUbicacion = buscadores.getBuscadorUbicacion();

        // ---------- CATEGORÍAS (por nombre) ----------
        if (inputDTO.getCategoria() != null && !inputDTO.getCategoria().isEmpty()) {
            List<FiltroCategoria> filtrosCategoria = inputDTO.getCategoria().stream()
                    .map(buscadorCategoria::buscar) // busca por nombre
                    .filter(Objects::nonNull)
                    .map(cat -> buscadorFiltro.buscarFiltroCategoriaPorCategoriaId(cat.getId())
                            .orElseGet(() -> new FiltroCategoria(cat)))
                    .toList();
            filtros.setFiltroCategoria(filtrosCategoria);
        }

        // ---------- CONTENIDO MULTIMEDIA ----------
        if (inputDTO.getContenidoMultimedia() != null && !inputDTO.getContenidoMultimedia().isEmpty()) {
            List<FiltroContenidoMultimedia> filtrosMultimedia = inputDTO.getContenidoMultimedia().stream()
                    .map(TipoContenido::fromCodigo)
                    .map(tipo -> buscadorFiltro.buscarFiltroContenidoMultimediaPorTipo(tipo)
                            .orElseGet(() -> new FiltroContenidoMultimedia(tipo)))
                    .toList();
            filtros.setFiltroContenidoMultimedia(filtrosMultimedia);
        }

        // ---------- DESCRIPCIÓN ----------
        if (inputDTO.getDescripcion() != null && !inputDTO.getDescripcion().isBlank()) {
            FiltroDescripcion filtro = buscadorFiltro.buscarFiltroDescripcionExacta(inputDTO.getDescripcion())
                    .orElseGet(() -> new FiltroDescripcion(inputDTO.getDescripcion()));
            filtros.setFiltroDescripcion(filtro);
        }

        // ---------- FECHA ACONTECIMIENTO ----------
        LocalDateTime faIni = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoInicial());
        LocalDateTime faFin = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoFinal());
        if (faIni != null && faFin != null) {
            FiltroFechaAcontecimiento filtro = buscadorFiltro
                    .buscarFiltroFechaAcontecimientoPorRango(faIni, faFin)
                    .orElseGet(() -> new FiltroFechaAcontecimiento(faIni, faFin));
            filtros.setFiltroFechaAcontecimiento(filtro);
        }

        // ---------- FECHA CARGA ----------
        LocalDateTime fcIni = FechaParser.parsearFecha(inputDTO.getFechaCargaInicial());
        LocalDateTime fcFin = FechaParser.parsearFecha(inputDTO.getFechaCargaFinal());
        if (fcIni != null && fcFin != null) {
            FiltroFechaCarga filtro = buscadorFiltro
                    .buscarFiltroFechaCargaPorRango(fcIni, fcFin)
                    .orElseGet(() -> new FiltroFechaCarga(fcIni, fcFin));
            filtros.setFiltroFechaCarga(filtro);
        }

        // ---------- PAÍSES (por nombre) ----------
        if (inputDTO.getPais() != null && !inputDTO.getPais().isEmpty()) {
            List<FiltroPais> filtrosPais = inputDTO.getPais().stream()
                    .map(buscadorPais::buscar) // busca por nombre
                    .filter(Objects::nonNull)
                    .map(pais -> buscadorFiltro.buscarFiltroPaisPorPaisId(pais.getId())
                            .orElseGet(() -> new FiltroPais(
                                    pais,
                                    buscadorUbicacion.buscarUbicacionesConPais(pais.getId()))))
                    .toList();
            filtros.setFiltroPais(filtrosPais);
        }

        // ---------- PROVINCIAS (por nombre) ----------
        if (inputDTO.getProvincia() != null && !inputDTO.getProvincia().isEmpty()) {
            List<FiltroProvincia> filtrosProvincia = inputDTO.getProvincia().stream()
                    .map(buscadorProvincia::buscar) // busca por nombre
                    .filter(Objects::nonNull)
                    .map(provincia -> buscadorFiltro.buscarFiltroProvinciaPorProvinciaId(provincia.getId())
                            .orElseGet(() -> new FiltroProvincia(
                                    provincia,
                                    buscadorUbicacion.buscarUbicacionesConProvincia(provincia.getId()))))
                    .toList();
            filtros.setFiltroProvincia(filtrosProvincia);
        }

        // ---------- TÍTULO ----------
        if (inputDTO.getTitulo() != null && !inputDTO.getTitulo().isBlank()) {
            FiltroTitulo filtro = buscadorFiltro.buscarFiltroTituloExacto(inputDTO.getTitulo())
                    .orElseGet(() -> new FiltroTitulo(inputDTO.getTitulo()));
            filtros.setFiltroTitulo(filtro);
        }

        return filtros;
    }

    public static FiltrosColeccion formatearFiltrosColeccionDinamica(
            BuscadoresRegistry buscadores,
            CriteriosColeccionDTO inputDTO) {

        FiltrosColeccion filtros = new FiltrosColeccion();

        BuscadorCategoria buscadorCategoria = buscadores.getBuscadorCategoria();
        BuscadorFiltro buscadorFiltro = buscadores.getBuscadorFiltro();
        BuscadorPais buscadorPais = buscadores.getBuscadorPais();
        BuscadorProvincia buscadorProvincia = buscadores.getBuscadorProvincia();
        BuscadorUbicacion buscadorUbicacion = buscadores.getBuscadorUbicacion();

        // ---------- CATEGORÍAS ----------
        if (inputDTO.getCategoriaId() != null && !inputDTO.getCategoriaId().isEmpty()) {
            List<FiltroCategoria> filtrosCategoria = inputDTO.getCategoriaId().stream()
                    .map(buscadorCategoria::buscar)
                    .filter(Objects::nonNull)
                    .map(categoria -> buscadorFiltro.buscarFiltroCategoriaPorCategoriaId(categoria.getId())
                            .orElseGet(() -> new FiltroCategoria(categoria)))
                    .toList();
            filtros.setFiltroCategoria(filtrosCategoria);
        }

        // ---------- CONTENIDO MULTIMEDIA ----------
        if (inputDTO.getContenidoMultimedia() != null && !inputDTO.getContenidoMultimedia().isEmpty()) {
            List<FiltroContenidoMultimedia> filtrosMultimedia = inputDTO.getContenidoMultimedia().stream()
                    .map(TipoContenido::fromCodigo)
                    .map(tipo -> buscadorFiltro.buscarFiltroContenidoMultimediaPorTipo(tipo)
                            .orElseGet(() -> new FiltroContenidoMultimedia(tipo)))
                    .toList();
            filtros.setFiltroContenidoMultimedia(filtrosMultimedia);
        }

        // ---------- Fuente ----------
        if (inputDTO.getFuentes() != null && !inputDTO.getFuentes().isEmpty()) {
            List<FiltroFuente> filtrosFuentes = inputDTO.getFuentes().stream()
                    .map(Fuente::fromCodigo)
                    .map(fuente -> buscadorFiltro.buscarFiltroFuentePorValor(fuente.getCodigo())
                            .orElseGet(() -> new FiltroFuente(fuente)))
                    .toList();
            filtros.setFiltroFuentes(filtrosFuentes);
        }

        // ---------- PAÍSES ----------
        
        if (inputDTO.getPaisId() != null && !inputDTO.getPaisId().isEmpty()) {
            List<FiltroPais> filtrosPais = inputDTO.getPaisId().stream()
                    .map(buscadorPais::buscar)
                    .filter(Objects::nonNull)
                    .map(pais -> buscadorFiltro.buscarFiltroPaisPorPaisId(pais.getId())
                            .orElseGet(() -> new FiltroPais(
                                    pais,
                                    buscadorUbicacion.buscarUbicacionesConPais(pais.getId()))))
                    .toList();

            filtrosPais.forEach(a -> a.refrescarUbicaciones_ids(buscadorUbicacion.buscarUbicacionesConPais(a.getPais().getId())));
            filtros.setFiltroPais(filtrosPais);
        }

        // ---------- PROVINCIAS ----------
        if (inputDTO.getProvinciaId() != null && !inputDTO.getProvinciaId().isEmpty()) {
            
            List<FiltroProvincia> filtrosProvincia = inputDTO.getProvinciaId().stream()
                    .map(buscadorProvincia::buscar)
                    .filter(Objects::nonNull)
                    .map(provincia -> buscadorFiltro.buscarFiltroProvinciaPorProvinciaId(provincia.getId())
                            .orElseGet(() -> new FiltroProvincia(
                                    provincia,
                                    buscadorUbicacion.buscarUbicacionesConProvincia(provincia.getId()))))
                    .toList();

            filtrosProvincia.forEach(a -> a.refrescarUbicaciones_ids(buscadorUbicacion.buscarUbicacionesConProvincia(a.getProvincia().getId())));
            filtros.setFiltroProvincia(filtrosProvincia);
        }

        // ---------- DESCRIPCIÓN ----------
        if (inputDTO.getDescripcion() != null && !inputDTO.getDescripcion().isBlank()) {
            FiltroDescripcion filtro = buscadorFiltro.buscarFiltroDescripcionExacta(inputDTO.getDescripcion())
                    .orElseGet(() -> new FiltroDescripcion(inputDTO.getDescripcion()));
            filtros.setFiltroDescripcion(filtro);
        }

        // ---------- FECHA ACONTECIMIENTO ----------
        LocalDateTime faIni = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoInicial());
        LocalDateTime faFin = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoFinal());
        if (faIni != null && faFin != null) {
            FiltroFechaAcontecimiento filtro = buscadorFiltro
                    .buscarFiltroFechaAcontecimientoPorRango(faIni, faFin)
                    .orElseGet(() -> new FiltroFechaAcontecimiento(faIni, faFin));
            filtros.setFiltroFechaAcontecimiento(filtro);
        }

        // ---------- FECHA CARGA ----------
        LocalDateTime fcIni = FechaParser.parsearFecha(inputDTO.getFechaCargaInicial());
        LocalDateTime fcFin = FechaParser.parsearFecha(inputDTO.getFechaCargaFinal());
        if (fcIni != null && fcFin != null) {
            FiltroFechaCarga filtro = buscadorFiltro
                    .buscarFiltroFechaCargaPorRango(fcIni, fcFin)
                    .orElseGet(() -> new FiltroFechaCarga(fcIni, fcFin));
            filtros.setFiltroFechaCarga(filtro);
        }

        // ---------- TÍTULO ----------
        if (inputDTO.getTitulo() != null && !inputDTO.getTitulo().isBlank()) {
            FiltroTitulo filtro = buscadorFiltro.buscarFiltroTituloExacto(inputDTO.getTitulo())
                    .orElseGet(() -> new FiltroTitulo(inputDTO.getTitulo()));
            filtros.setFiltroTitulo(filtro);
        }

        return filtros;
    }


    public static CriteriosColeccionDTO filtrosColeccionToString(List<Filtro> filtros) {

        CriteriosColeccionDTO criterios = new CriteriosColeccionDTO();

        if (filtros == null || filtros.isEmpty()) {
            return criterios;
        }

        // Inicializamos TODAS las listas
        criterios.setCategoria(new ArrayList<>());
        criterios.setCategoriaId(new ArrayList<>());
        criterios.setPais(new ArrayList<>());
        criterios.setPaisId(new ArrayList<>());
        criterios.setProvincia(new ArrayList<>());
        criterios.setProvinciaId(new ArrayList<>());
        criterios.setFuentes(new ArrayList<>());
        criterios.setContenidoMultimedia(new ArrayList<>());

        for (Filtro filtro : filtros) {

            // ---------- CATEGORÍA ----------
            if (filtro instanceof FiltroCategoria filtroCategoria) {
                Categoria categoriaObj = filtroCategoria.getCategoria();
                if (categoriaObj != null) {
                    criterios.getCategoria().add(categoriaObj.getTitulo());
                    criterios.getCategoriaId().add(categoriaObj.getId());
                }
            }

            // ---------- CONTENIDO MULTIMEDIA ----------
            else if (filtro instanceof FiltroContenidoMultimedia filtroContenido) {
                TipoContenido contenido = filtroContenido.getTipoContenido();
                if (contenido != null) {
                    criterios.getContenidoMultimedia().add(contenido.getCodigo());
                }
            }

            // ---------- DESCRIPCIÓN ----------
            else if (filtro instanceof FiltroDescripcion filtroDescripcion) {
                criterios.setDescripcion(filtroDescripcion.getDescripcion());
            }

            // ---------- FECHA ACONTECIMIENTO ----------
            else if (filtro instanceof FiltroFechaAcontecimiento filtroFecha) {
                criterios.setFechaAcontecimientoInicial(
                        filtroFecha.getFechaInicial() != null ? filtroFecha.getFechaInicial().toString() : null);
                criterios.setFechaAcontecimientoFinal(
                        filtroFecha.getFechaFinal() != null ? filtroFecha.getFechaFinal().toString() : null);
            }

            // ---------- FECHA CARGA ----------
            else if (filtro instanceof FiltroFechaCarga filtroFechaCarga) {
                criterios.setFechaCargaInicial(
                        filtroFechaCarga.getFechaInicial() != null ? filtroFechaCarga.getFechaInicial().toString() : null);
                criterios.setFechaCargaFinal(
                        filtroFechaCarga.getFechaFinal() != null ? filtroFechaCarga.getFechaFinal().toString() : null);
            }

            // ---------- FUENTE ----------
            else if (filtro instanceof FiltroFuente filtroFuente) {
                Fuente fuente = filtroFuente.getFuenteDeseada();
                if (fuente != null) {
                    criterios.getFuentes().add(fuente.getCodigo());
                }
            }

            // ---------- PAÍS ----------
            else if (filtro instanceof FiltroPais filtroPais) {
                Pais pais = filtroPais.getPais();
                if (pais != null) {
                    criterios.getPais().add(pais.getPais());
                    criterios.getPaisId().add(pais.getId());
                }
            }

            // ---------- PROVINCIA ----------
            else if (filtro instanceof FiltroProvincia filtroProvincia) {
                Provincia provincia = filtroProvincia.getProvincia();
                if (provincia != null) {
                    criterios.getProvincia().add(provincia.getProvincia());
                    criterios.getProvinciaId().add(provincia.getId());
                }
            }

            // ---------- TÍTULO ----------
            else if (filtro instanceof FiltroTitulo filtroTitulo) {
                criterios.setTitulo(filtroTitulo.getTitulo());
            }
        }

        return criterios;
    }




    private static <T extends IFiltro> void agregarSiNoVacia(
            List<List<IFiltro>> filtrosPorCategoria,
            List<T> filtros) {

        if (filtros != null && !filtros.isEmpty()) {
            List<IFiltro> lista = filtros.stream()
                    .map(f -> (IFiltro) f)
                    .collect(Collectors.toCollection(ArrayList::new));
            filtrosPorCategoria.add(lista);
        }
    }



    public static List<List<IFiltro>> obtenerListaDeFiltros(FiltrosColeccion filtrosColeccion) {
        List<List<IFiltro>> filtrosPorCategoria = new ArrayList<>();

        List<IFiltro> filtrosIndividual = new ArrayList<>();

        if (filtrosColeccion!=null){
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroCategoria());
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroPais());
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroProvincia());
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroFuentes());
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroContenidoMultimedia());
            if (filtrosColeccion.getFiltroDescripcion()!=null)
                filtrosIndividual.add(filtrosColeccion.getFiltroDescripcion());
            if (filtrosColeccion.getFiltroFechaAcontecimiento()!=null)
                filtrosIndividual.add(filtrosColeccion.getFiltroFechaAcontecimiento());
            if (filtrosColeccion.getFiltroFechaCarga()!=null)
                filtrosIndividual.add(filtrosColeccion.getFiltroFechaCarga());
            if (filtrosColeccion.getFiltroTitulo()!=null)
                filtrosIndividual.add(filtrosColeccion.getFiltroTitulo());

            agregarSiNoVacia(filtrosPorCategoria,filtrosIndividual);
        }

        return filtrosPorCategoria;
    }
}
