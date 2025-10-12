package modulos.agregacion.entities.fuentes;

import modulos.agregacion.entities.DbMain.*;
import modulos.agregacion.entities.DbMain.hechoRef.HechoRef;
import modulos.agregacion.entities.DbProxy.HechoProxy;
import modulos.agregacion.entities.atributosHecho.AtributosHecho;
import modulos.agregacion.entities.atributosHecho.Origen;
import modulos.agregacion.entities.fuentes.Requests.*;
import modulos.shared.dtos.output.ColeccionOutputDTO;
import modulos.shared.dtos.output.VisualizarHechosOutputDTO;
import modulos.shared.utils.FormateadorHecho;
import modulos.buscadores.*;
import modulos.shared.dtos.input.*;
import modulos.shared.utils.FechaParser;
import modulos.shared.utils.Geocodificador;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class FuenteProxy {

    WebClient webClient = WebClient.create("https://api-ddsi.disilab.ar/public/api");
    WebClient webClientMetaMapa = WebClient.create("http://localhost:8080");

    public Mono<String> login(String email, String contrasenia){

            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(contrasenia);

            // devolvés el token
            return webClient.post()
                    .uri("/login")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Error 4xx: " + body))
                                    .flatMap(Mono::error)
                    )
                    .bodyToMono(LoginResponse.class)
                    .map(LoginResponse::getToken);
    }

        public Mono<List<Hecho>> getHechos(BuscadoresRegistry buscadores, String token) {
        //TODO no se si esta bien que el token vaya en el query param
            List<Hecho> hechos = new ArrayList<>();

                return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/desastres")
                                .queryParam("token", token).build())
                        .retrieve()
                        .bodyToMono(HechosResponse.class).map(response -> {
                            for(HechoResponse hechoResponse : response.getHechos()){
                            Hecho hecho = this.setearHecho(hechoResponse, buscadores);
                            hechos.add(hecho);
                        }
                            return hechos;
                        });
    }

    private Hecho setearHecho(HechoResponse hechoResponse, BuscadoresRegistry buscadores) {
        HechoProxy hecho = new HechoProxy();
        hecho.getAtributosHecho().setTitulo(hechoResponse.getTitulo());
        hecho.getAtributosHecho().setDescripcion(hechoResponse.getDescripcion());
        Categoria categoria = buscadores.getBuscadorCategoria().buscar(hechoResponse.getCategoria());
        hecho.getAtributosHecho().setCategoria_id(categoria != null ? categoria.getId() : null);
        UbicacionString ubicacionString = Geocodificador.obtenerUbicacion(hechoResponse.getLatitud(), hechoResponse.getLongitud());
        if(ubicacionString != null) {
            Pais pais = buscadores.getBuscadorPais().buscar(ubicacionString.getPais());
            Provincia provincia = buscadores.getBuscadorProvincia().buscar(ubicacionString.getProvincia());
            Ubicacion ubicacion = buscadores.getBuscadorUbicacion().buscarOCrear(pais, provincia);
            hecho.getAtributosHecho().setUbicacion_id(ubicacion != null ? ubicacion.getId() : null);
            hecho.getAtributosHecho().setLatitud(hechoResponse.getLatitud());
            hecho.getAtributosHecho().setLatitud(hechoResponse.getLongitud());
        }
        hecho.getAtributosHecho().setOrigen(Origen.FUENTE_PROXY_METAMAPA);
        hecho.getAtributosHecho().setFuente(Fuente.PROXY);
        hecho.getAtributosHecho().setFechaAcontecimiento(FechaParser.parsearFecha(hechoResponse.getFecha_hecho()));
        hecho.getAtributosHecho().setFechaCarga(FechaParser.parsearFecha(hechoResponse.getCreated_at()));
        hecho.getAtributosHecho().setFechaUltimaActualizacion(FechaParser.parsearFecha(hechoResponse.getUpdated_at()));
        hecho.getAtributosHecho().setModificado(true);
        return hecho;
    }

    public Mono<Hecho> getHechoPorId(Long id, BuscadoresRegistry buscadores, String token) {

            return webClient.get()
                    .uri(UriBuilder -> UriBuilder.path("/desastres-naturales")
                            .queryParam("token",token)
                            .queryParam("id", id)
                            .build())
                            .retrieve()
                            .bodyToMono(HechoResponse.class)
                            .map(hechoResponse -> this.setearHecho(hechoResponse, buscadores));
    }

    public Mono<List<Coleccion>> getColeccionesMetaMapa(BuscadoresRegistry buscadores){

        List<Coleccion> colecciones = new ArrayList<>();

        return webClientMetaMapa.get().uri("/api/coleccion/get-all")
                .retrieve()
                .bodyToMono(ColeccionesResponse.class)
                .map(coleccionesResponse -> {
                    for(ColeccionOutputDTO coleccionResponse : coleccionesResponse.getColecciones()){
                        Coleccion coleccion = new Coleccion();
                        coleccion.setTitulo(coleccionResponse.getTitulo());
                        coleccion.setDescripcion(coleccionResponse.getDescripcion());
                        coleccion.setActivo(true);
                        coleccion.setModificado(true);
                        coleccion.setCriterios(FormateadorHecho.obtenerListaDeFiltros(FormateadorHecho.formatearFiltrosColeccion(buscadores, coleccionResponse.getCriterios())));
                        //TODO falta ver como verga se recibe lo de navegacion curada, me dio paja y lo hardcodee en true
                        Mono<List<HechoRef>> hechosRefMono = this.getHechosDeColeccionMetaMapa(
                                coleccionResponse.getCriterios(),
                                true,
                                coleccionResponse.getId(),
                                buscadores
                        ).map(hechosList -> {
                            List<HechoRef> hechosRef = new ArrayList<>();
                            for (Hecho h : hechosList) {
                                h.getAtributosHecho().setFuente(Fuente.PROXY);
                                hechosRef.add(new HechoRef(h.getId(), Fuente.PROXY));
                            }
                            coleccion.setHechos(hechosRef);
                            colecciones.add(coleccion);
                            return hechosRef;
                        });
                    }
                    return colecciones;
                });
    }

    public Mono<List<Hecho>> getHechosMetaMapa(BuscadoresRegistry buscadores){

        List<Hecho> hechos = new ArrayList<>();

        return webClientMetaMapa.get().uri("/api/hechos/get-all")
                .retrieve()
                .bodyToMono(HechosMetamapaResponse.class)
                .map(hechosResponse -> {
                            for (VisualizarHechosOutputDTO hechoDto : hechosResponse.getHechos()) {
                                hechos.add(this.setearHechoMetamapa(hechoDto, buscadores));
                            }
                            return hechos;
                        }
                );
    }

    private Hecho setearHechoMetamapa(VisualizarHechosOutputDTO hechoDto, BuscadoresRegistry buscadores){
        Hecho hecho = new HechoProxy();
        hecho.setAtributosHecho(new AtributosHecho());
        hecho.getAtributosHecho().setFuente(Fuente.valueOf(hechoDto.getFuente()));
        //hecho.setContenidoMultimedia(dto.getContenidoMultimedia()); TODO

        // Crear y setear atributosHecho
        AtributosHecho atributos = new AtributosHecho();
        atributos.setTitulo(hechoDto.getTitulo());
        atributos.setDescripcion(hechoDto.getDescripcion());
        atributos.setCategoria_id(buscadores.getBuscadorCategoria().buscar(hechoDto.getCategoria()).getId());

        Pais pais = buscadores.getBuscadorPais().buscar(hechoDto.getPais());
        Provincia provincia = buscadores.getBuscadorProvincia().buscar(hechoDto.getProvincia());

        if(pais != null && provincia != null){
            Ubicacion ubicacion = buscadores.getBuscadorUbicacion().buscar(pais.getId(), provincia.getId());
            if(ubicacion != null){
                atributos.setUbicacion_id(ubicacion.getId());
            }
        }
        Categoria categoria = buscadores.getBuscadorCategoria().buscar(hechoDto.getCategoria());

        if(categoria != null){
            atributos.setCategoria_id(categoria.getId());
        }

        atributos.setFechaAcontecimiento(FechaParser.parsearFecha(hechoDto.getFechaAcontecimiento()));
        atributos.setLatitud(hechoDto.getLatitud());
        atributos.setLongitud(hechoDto.getLongitud());

        hecho.getAtributosHecho().setOrigen(Origen.FUENTE_PROXY_METAMAPA);
        hecho.getAtributosHecho().setFuente(Fuente.PROXY);

        return hecho;
    }

    public Mono<List<Hecho>> getHechosDeColeccionMetaMapa(CriteriosColeccionProxyDTO atributos, Boolean navegacionCurada, Long id_coleccion, BuscadoresRegistry buscadores) {

        GetHechosColeccionInputDTO request = new GetHechosColeccionInputDTO();
        request.setId_coleccion(id_coleccion);
        request.setNavegacionCurada(navegacionCurada);
        request.setOrigen(Origen.valueOf(atributos.getOrigen()).getCodigo());
        request.setDescripcion(atributos.getDescripcion());
        request.setTitulo(atributos.getTitulo());
        request.setFechaAcontecimientoInicial(atributos.getFechaAcontecimientoInicial());
        request.setFechaAcontecimientoFinal(atributos.getFechaAcontecimientoFinal());
        request.setFechaCargaInicial(atributos.getFechaCargaInicial());
        request.setFechaCargaFinal(atributos.getFechaCargaFinal());
        request.setPaisId(buscadores.getBuscadorPais().buscar(atributos.getPais()).getId());
        request.setProvinciaId(buscadores.getBuscadorProvincia().buscar(atributos.getProvincia()).getId());

        List<Hecho> hechos = new ArrayList<>();

        return webClientMetaMapa.post().uri("/api/hechos/get-all")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(HechosSkibidi.class)
                .map(hechosSkibidi -> {
                    for (VisualizarHechosOutputDTO dto : hechosSkibidi.getHechos()) {
                        hechos.add(this.setearHechoMetamapa(dto, buscadores));
                    }
                    return hechos;
                });
    }

    public void enviarReporte(Long id_hecho, String motivo){

            webClientMetaMapa.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/solicitud-hecho/reportar")
                            .queryParam("id_hecho", id_hecho)
                            .queryParam("motivo", motivo).build())
                    .retrieve()
                    .toBodilessEntity() // <- indica que no se espera body
                    .subscribe();
    }

    public void enviarSolicitudEliminacionMetaMapa(SolicitudHechoEliminarInputDTO data){

        webClientMetaMapa.post()
                .uri("/api/solicitud-hecho/eliminar-hecho")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(data)
                .retrieve()
                .toBodilessEntity()
                .subscribe();

    }

}








