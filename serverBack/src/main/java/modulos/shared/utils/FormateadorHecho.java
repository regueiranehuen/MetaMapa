package modulos.shared.utils;

import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.entities.atributosHecho.Origen;
import modulos.agregacion.entities.atributosHecho.TipoContenido;
import modulos.buscadores.*;
import modulos.shared.dtos.input.CriteriosColeccionDTO;
import modulos.shared.dtos.input.SolicitudHechoInputDTO;
import modulos.agregacion.entities.DbMain.filtros.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class FormateadorHecho {

/* FUNCIONES ÑOQUI
    public static <T extends Hecho> T formatearHechoBDD(HechoMemoria hecho, Class<T> tipo){
        AtributosHecho atributos = AtributosHecho.builder()
                .categoria_id(hecho.getAtributosHecho().getCategoria().getId())
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud())
                .longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidosMultimedia(hecho.getAtributosHecho().getContenidoMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion_id(hecho.getAtributosHecho().getUbicacion().getId())
                .build();

        List<AtributosHechoModificar> listaAtributosHechoModificar = new ArrayList<>();

        for (AtributosHechoModificarMemoria atributosModificar : hecho.getAtributosHechoAModificar()){
            AtributosHechoModificar atributos123 = AtributosHechoModificar.builder()
                    .id(atributosModificar.getId())
                    .latitud(atributosModificar.getLatitud())
                    .longitud(atributosModificar.getLongitud())
                    .categoria_id(atributosModificar.getCategoria().getId())
                    .titulo(atributosModificar.getTitulo())
                    .contenidoMultimedia(atributosModificar.getContenidoMultimedia())
                    .fechaAcontecimiento(atributosModificar.getFechaAcontecimiento())
                    .ubicacion_id(atributosModificar.getUbicacion().getId())
                    .build();
            listaAtributosHechoModificar.add(atributos123);
        }

        Hecho hecho123 = null;

        if (tipo == HechoDinamica.class){

            hecho123 = HechoDinamica.builder()
                        .id(hecho.getId())
                        .activo(hecho.getActivo())
                        .usuario_id(hecho.getUsuario_id())
                        .atributosHecho(atributos)
                        .atributosHechoAModificar(listaAtributosHechoModificar)
                        .build();

        }
        else if (tipo == HechoEstatica.class){
            hecho123 = HechoEstatica.builder()
                        .id(hecho.getId())
                        .activo(hecho.getActivo())
                        .usuario_id(hecho.getUsuario_id())
                        .datasets(hecho.getDatasets())
                        .atributosHecho(atributos)
                        .atributosHechoAModificar(listaAtributosHechoModificar)
                        .build();
        }
        else if (tipo == HechoProxy.class){
            hecho123 = HechoProxy.builder()
                    .id(hecho.getId())
                    .activo(hecho.getActivo())
                    .usuario_id(hecho.getUsuario_id())
                    .atributosHecho(atributos)
                    .atributosHechoAModificar(listaAtributosHechoModificar)
                    .build();
        }
        else{
            // Nunca debería entrar acá
            return null;
        }

        return tipo.cast(hecho123);
    }

    public static Hecho formatearHechoBDD(HechoMemoria hecho){
        AtributosHecho atributos = AtributosHecho.builder()
                .categoria_id(hecho.getAtributosHecho().getCategoria().getId())
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud())
                .longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidosMultimedia(hecho.getAtributosHecho().getContenidoMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion_id(hecho.getAtributosHecho().getUbicacion().getId())
                .build();

        List<AtributosHechoModificar> listaAtributosHechoModificar = new ArrayList<>();

        for (AtributosHechoModificarMemoria atributosModificar : hecho.getAtributosHechoAModificar()){
            AtributosHechoModificar atributos123 = AtributosHechoModificar.builder()
                    .id(atributosModificar.getId())
                    .latitud(atributosModificar.getLatitud())
                    .longitud(atributosModificar.getLongitud())
                    .categoria_id(atributosModificar.getCategoria().getId())
                    .titulo(atributosModificar.getTitulo())
                    .contenidoMultimedia(atributosModificar.getContenidoMultimedia())
                    .fechaAcontecimiento(atributosModificar.getFechaAcontecimiento())
                    .ubicacion_id(atributosModificar.getUbicacion().getId())
                    .build();
            listaAtributosHechoModificar.add(atributos123);
        }

        Hecho hecho123 = null;

        switch (hecho.getAtributosHecho().getOrigen()){
            case CARGA_MANUAL, FUENTE_DINAMICA: {
                hecho123 = HechoDinamica.builder()
                        .id(hecho.getId())
                        .activo(hecho.getActivo())
                        .usuario_id(hecho.getUsuario_id())
                        .atributosHecho(atributos)
                        .atributosHechoAModificar(listaAtributosHechoModificar)
                        .build();
                break;
            }
            case FUENTE_ESTATICA: {
                hecho123 = HechoEstatica.builder()
                        .id(hecho.getId())
                        .activo(hecho.getActivo())
                        .usuario_id(hecho.getUsuario_id())
                        .datasets(hecho.getDatasets())
                        .atributosHecho(atributos)
                        .atributosHechoAModificar(listaAtributosHechoModificar)
                        .build();
                break;
            }
            case FUENTE_PROXY_METAMAPA:{
                hecho123 = HechoProxy.builder()
                        .id(hecho.getId())
                        .activo(hecho.getActivo())
                        .usuario_id(hecho.getUsuario_id())
                        .atributosHecho(atributos)
                        .atributosHechoAModificar(listaAtributosHechoModificar)
                        .build();
                break;
            }
            default:
                System.out.println("Nunca voy a entrar acá (?)");
                return null;
        }

        return hecho123;
    }

    public static HechoEstatica formatearHechoEstaticaBDD(HechoMemoria hecho){
        AtributosHecho atributos = AtributosHecho.builder()
                .categoria_id(hecho.getAtributosHecho().getCategoria().getId())
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud())
                .longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidosMultimedia(hecho.getAtributosHecho().getContenidoMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion_id(hecho.getAtributosHecho().getUbicacion().getId())
                .build();

        List<AtributosHechoModificar> listaAtributosHechoModificar = new ArrayList<>();

        for (AtributosHechoModificarMemoria atributosModificar : hecho.getAtributosHechoAModificar()){
            AtributosHechoModificar atributos123 = AtributosHechoModificar.builder()
                    .id(atributosModificar.getId())
                    .latitud(atributosModificar.getLatitud())
                    .longitud(atributosModificar.getLongitud())
                    .categoria_id(atributosModificar.getCategoria().getId())
                    .titulo(atributosModificar.getTitulo())
                    .contenidoMultimedia(atributosModificar.getContenidoMultimedia())
                    .fechaAcontecimiento(atributosModificar.getFechaAcontecimiento())
                    .ubicacion_id(atributosModificar.getUbicacion().getId())
                    .build();
            listaAtributosHechoModificar.add(atributos123);
        }


        return HechoEstatica.builder()
                .id(hecho.getId())
                .activo(hecho.getActivo())
                .usuario_id(hecho.getUsuario_id())
                .datasets(hecho.getDatasets())
                .atributosHecho(atributos)
                .atributosHechoAModificar(listaAtributosHechoModificar)
                .build();
    }

    public static HechoDinamica formatearHechoDinamicaBDD(HechoMemoria hecho){
        AtributosHecho atributos = AtributosHecho.builder()
                .categoria_id(hecho.getAtributosHecho().getCategoria().getId())
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud())
                .longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidosMultimedia(hecho.getAtributosHecho().getContenidoMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion_id(hecho.getAtributosHecho().getUbicacion().getId())
                .build();

        List<AtributosHechoModificar> listaAtributosHechoModificar = new ArrayList<>();

        for (AtributosHechoModificarMemoria atributosModificar : hecho.getAtributosHechoAModificar()){
            AtributosHechoModificar atributos123 = AtributosHechoModificar.builder()
                    .id(atributosModificar.getId())
                    .latitud(atributosModificar.getLatitud())
                    .longitud(atributosModificar.getLongitud())
                    .categoria_id(atributosModificar.getCategoria().getId())
                    .titulo(atributosModificar.getTitulo())
                    .contenidoMultimedia(atributosModificar.getContenidoMultimedia())
                    .fechaAcontecimiento(atributosModificar.getFechaAcontecimiento())
                    .ubicacion_id(atributosModificar.getUbicacion().getId())
                    .build();
            listaAtributosHechoModificar.add(atributos123);
        }


        return HechoDinamica.builder()
                .id(hecho.getId())
                .activo(hecho.getActivo())
                .usuario_id(hecho.getUsuario_id())
                .atributosHecho(atributos)
                .atributosHechoAModificar(listaAtributosHechoModificar)
                .build();
    }

    public static HechoProxy formatearHechoProxyBDD(HechoMemoria hecho){
        AtributosHecho atributos = AtributosHecho.builder()
                .categoria_id(hecho.getAtributosHecho().getCategoria().getId())
                .descripcion(hecho.getAtributosHecho().getDescripcion())
                .latitud(hecho.getAtributosHecho().getLatitud())
                .longitud(hecho.getAtributosHecho().getLongitud())
                .modificado(hecho.getAtributosHecho().getModificado())
                .fechaCarga(hecho.getAtributosHecho().getFechaCarga())
                .origen(hecho.getAtributosHecho().getOrigen())
                .fechaAcontecimiento(hecho.getAtributosHecho().getFechaAcontecimiento())
                .fechaUltimaActualizacion(hecho.getAtributosHecho().getFechaUltimaActualizacion())
                .contenidosMultimedia(hecho.getAtributosHecho().getContenidoMultimedia())
                .titulo(hecho.getAtributosHecho().getTitulo())
                .ubicacion_id(hecho.getAtributosHecho().getUbicacion().getId())
                .build();

        List<AtributosHechoModificar> listaAtributosHechoModificar = new ArrayList<>();

        for (AtributosHechoModificarMemoria atributosModificar : hecho.getAtributosHechoAModificar()){
            AtributosHechoModificar atributos123 = AtributosHechoModificar.builder()
                    .id(atributosModificar.getId())
                    .latitud(atributosModificar.getLatitud())
                    .longitud(atributosModificar.getLongitud())
                    .categoria_id(atributosModificar.getCategoria().getId())
                    .titulo(atributosModificar.getTitulo())
                    .contenidoMultimedia(atributosModificar.getContenidoMultimedia())
                    .fechaAcontecimiento(atributosModificar.getFechaAcontecimiento())
                    .ubicacion_id(atributosModificar.getUbicacion().getId())
                    .build();
            listaAtributosHechoModificar.add(atributos123);
        }


        return HechoProxy.builder()
                .id(hecho.getId())
                .activo(hecho.getActivo())
                .usuario_id(hecho.getUsuario_id())
                .atributosHecho(atributos)
                .atributosHechoAModificar(listaAtributosHechoModificar)
                .build();
    }

*/

    public static AtributosHecho formatearAtributosHecho(BuscadoresRegistry buscadores, SolicitudHechoInputDTO dtoInput){

    AtributosHecho atributos = new AtributosHecho();

    Pais pais = null;
    Provincia provincia = null;

    if (dtoInput.getId_pais() != null){
        pais = buscadores.getBuscadorPais().buscar(dtoInput.getId_pais());
        if (pais != null)
            System.out.println("SOY UN PAIS FELIZ: " + pais.getPais());
    }

    if (dtoInput.getId_provincia() != null){
        provincia = buscadores.getBuscadorProvincia().buscar(dtoInput.getId_provincia());

        if (pais != null)
            System.out.println("SOY UNA PROVINCIA FELIZ: " + provincia.getProvincia());
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
        ZonedDateTime faIni = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoInicial());
        ZonedDateTime faFin = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoFinal());
        if (faIni != null && faFin != null) {
            FiltroFechaAcontecimiento filtro = buscadorFiltro
                    .buscarFiltroFechaAcontecimientoPorRango(faIni, faFin)
                    .orElseGet(() -> new FiltroFechaAcontecimiento(faIni, faFin));
            filtros.setFiltroFechaAcontecimiento(filtro);
        }

        // ---------- FECHA CARGA ----------
        ZonedDateTime fcIni = FechaParser.parsearFecha(inputDTO.getFechaCargaInicial());
        ZonedDateTime fcFin = FechaParser.parsearFecha(inputDTO.getFechaCargaFinal());
        if (fcIni != null && fcFin != null) {
            FiltroFechaCarga filtro = buscadorFiltro
                    .buscarFiltroFechaCargaPorRango(fcIni, fcFin)
                    .orElseGet(() -> new FiltroFechaCarga(fcIni, fcFin));
            filtros.setFiltroFechaCarga(filtro);
        }

        // ---------- ORIGEN ----------
        if (inputDTO.getOrigen() != null && !inputDTO.getOrigen().isEmpty()) {
            List<FiltroOrigen> filtrosOrigen = inputDTO.getOrigen().stream()
                    .map(Origen::fromCodigo)
                    .map(origen -> buscadorFiltro.buscarFiltroOrigenPorValor(origen.getCodigo())
                            .orElseGet(() -> new FiltroOrigen(origen)))
                    .toList();
            filtros.setFiltroOrigen(filtrosOrigen);
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


    //TODO interface formateador ?)
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

        // ---------- ORIGEN ----------
        if (inputDTO.getOrigen() != null && !inputDTO.getOrigen().isEmpty()) {
            List<FiltroOrigen> filtrosOrigen = inputDTO.getOrigen().stream()
                    .map(Origen::fromCodigo)
                    .map(origen -> buscadorFiltro.buscarFiltroOrigenPorValor(origen.getCodigo())
                            .orElseGet(() -> new FiltroOrigen(origen)))
                    .toList();
            filtros.setFiltroOrigen(filtrosOrigen);
        }

        // ---------- PAÍSES ----------
        System.out.println("VOY A ENTRAR A PAIS EN FORMAT");
        if (inputDTO.getPaisId() != null && !inputDTO.getPaisId().isEmpty()) {

            List<FiltroPais> filtrosPais = inputDTO.getPaisId().stream()
                    .map(buscadorPais::buscar)
                    .filter(Objects::nonNull)
                    .map(pais -> buscadorFiltro.buscarFiltroPaisPorPaisId(pais.getId())
                            .orElseGet(() -> new FiltroPais(
                                    pais,
                                    buscadorUbicacion.buscarUbicacionesConPais(pais.getId()))))
                    .toList();

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
            for(FiltroProvincia filtro : filtrosProvincia){
                System.out.println("Filtro Provincia: " + filtro.getProvincia().getProvincia());
                for(Long id : filtro.getUbicaciones_ids()){
                    System.out.println("Ubicaciones ids:" + id);
                }
            }

            filtros.setFiltroProvincia(filtrosProvincia);
        }

        // ---------- DESCRIPCIÓN ----------
        if (inputDTO.getDescripcion() != null && !inputDTO.getDescripcion().isBlank()) {
            FiltroDescripcion filtro = buscadorFiltro.buscarFiltroDescripcionExacta(inputDTO.getDescripcion())
                    .orElseGet(() -> new FiltroDescripcion(inputDTO.getDescripcion()));
            filtros.setFiltroDescripcion(filtro);
        }

        // ---------- FECHA ACONTECIMIENTO ----------
        ZonedDateTime faIni = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoInicial());
        ZonedDateTime faFin = FechaParser.parsearFecha(inputDTO.getFechaAcontecimientoFinal());
        if (faIni != null && faFin != null) {
            FiltroFechaAcontecimiento filtro = buscadorFiltro
                    .buscarFiltroFechaAcontecimientoPorRango(faIni, faFin)
                    .orElseGet(() -> new FiltroFechaAcontecimiento(faIni, faFin));
            filtros.setFiltroFechaAcontecimiento(filtro);
        }

        // ---------- FECHA CARGA ----------
        ZonedDateTime fcIni = FechaParser.parsearFecha(inputDTO.getFechaCargaInicial());
        ZonedDateTime fcFin = FechaParser.parsearFecha(inputDTO.getFechaCargaFinal());
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

        if (filtros!=null){
            // Inicializamos las listas vacías
            criterios.setCategoria(new ArrayList<>());
            criterios.setPais(new ArrayList<>());
            criterios.setProvincia(new ArrayList<>());
            criterios.setOrigen(new ArrayList<>());
            criterios.setContenidoMultimedia(new ArrayList<>());

            for (Filtro filtro : filtros) {

                // ---------- CATEGORÍA ----------
                if (filtro instanceof FiltroCategoria filtroCategoria) {
                    Categoria categoriaObj = filtroCategoria.getCategoria();
                    if (categoriaObj != null)
                        criterios.getCategoria().add(categoriaObj.getTitulo());
                }

                // ---------- CONTENIDO MULTIMEDIA ----------
                else if (filtro instanceof FiltroContenidoMultimedia filtroContenido) {
                    TipoContenido contenido = filtroContenido.getTipoContenido();
                    if (contenido != null)
                        criterios.getContenidoMultimedia().add(contenido.getCodigo());
                }

                // ---------- DESCRIPCIÓN ----------
                else if (filtro instanceof FiltroDescripcion filtroDescripcion) {
                    criterios.setDescripcion(filtroDescripcion.getDescripcion());
                }

                // ---------- FECHA ACONTECIMIENTO ----------
                else if (filtro instanceof FiltroFechaAcontecimiento filtroFecha) {
                    criterios.setFechaAcontecimientoInicial(filtroFecha.getFechaInicial().toString());
                    criterios.setFechaAcontecimientoFinal(filtroFecha.getFechaFinal().toString());
                }

                // ---------- FECHA CARGA ----------
                else if (filtro instanceof FiltroFechaCarga filtroFechaCarga) {
                    criterios.setFechaCargaInicial(filtroFechaCarga.getFechaInicial().toString());
                    criterios.setFechaCargaFinal(filtroFechaCarga.getFechaFinal().toString());
                }

                // ---------- ORIGEN ----------
                else if (filtro instanceof FiltroOrigen filtroOrigen) {
                    Origen origen = filtroOrigen.getOrigenDeseado();
                    if (origen != null)
                        criterios.getOrigen().add(origen.getCodigo());
                }

                // ---------- PAÍS ----------
                else if (filtro instanceof FiltroPais filtroPais) {
                    Pais pais = filtroPais.getPais();
                    if (pais != null)
                        criterios.getPais().add(pais.getPais());
                }

                // ---------- PROVINCIA ----------
                else if (filtro instanceof FiltroProvincia filtroProvincia) {
                    Provincia provincia = filtroProvincia.getProvincia();
                    if (provincia != null)
                        criterios.getProvincia().add(provincia.getProvincia());
                }

                // ---------- TÍTULO ----------
                else if (filtro instanceof FiltroTitulo filtroTitulo) {
                    criterios.setTitulo(filtroTitulo.getTitulo());
                }
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
                    .collect(Collectors.toCollection(ArrayList::new)); // ✅ mutable
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
            agregarSiNoVacia(filtrosPorCategoria, filtrosColeccion.getFiltroOrigen());
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
