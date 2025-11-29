package modulos.Front.services;

import jakarta.servlet.http.HttpSession;
import modulos.Front.ContenidoMultimedia;
import modulos.Front.dtos.input.HechoModificarInputDTO;
import modulos.Front.dtos.output.*;
import modulos.Front.dtos.input.GetHechosColeccionInputDTO;
import modulos.Front.dtos.input.ImportacionHechosInputDTO;
import modulos.Front.dtos.input.SolicitudHechoInputDTO;
import modulos.Front.sessionHandlers.ActiveSessionTracker;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class HechosService {
    private final WebApiCallerService webApiCallerService;
    private String hechoServiceUrl = "/api/hechos";
    private final ActiveSessionTracker activeSessionTracker;

    public HechosService(WebApiCallerService webApiCallerService, ActiveSessionTracker activeSessionTracker){
        this.webApiCallerService = webApiCallerService;
        this.activeSessionTracker = activeSessionTracker;
    }


    public ResponseEntity<?> getHecho(Long id_hecho, String fuente) {
        return webApiCallerService.getEntityTokenOpcional(this.hechoServiceUrl + "/public/get?id_hecho=" + id_hecho + "&fuente=" + fuente, VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> subirHecho(SolicitudHechoInputDTO hechoInputDTO) {
        return webApiCallerService.postMultipartHecho(this.hechoServiceUrl + "/subir", hechoInputDTO, Void.class);
    }

    public Mono<ResponseEntity<Void>> importarHechos(ImportacionHechosInputDTO dto,
                                                     ByteArrayResource fileResource,
                                                     String contentType){
        return webApiCallerService.importarHecho(dto, fileResource, contentType);
    }

    public ResponseEntity<?> getHechos() {
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/get-all?origen=0", VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> getHechosFiltradosColeccion(GetHechosColeccionInputDTO inputDTO) {
        return webApiCallerService.postListTokenOpcional(this.hechoServiceUrl + "/public/get/filtrar", inputDTO, VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> getHechosConLatitudYLongitud(){
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/get-mapa?origen=0", VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> getPaises() {
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/paises/get-all", PaisDto.class);
    }

    public ResponseEntity<?> getProvinciasByIdPais(Long id_pais) {
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/provincias?id_pais="+id_pais, ProvinciaDto.class);
    }

    public ResponseEntity<?> getCategorias() {
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/categorias/get-all", CategoriaDto.class);
    }

    public ResponseEntity<Long> getCantHechos() {
        return webApiCallerService.getEntityTokenOpcional(this.hechoServiceUrl + "/public/cantHechos", Long.class);
    }

    public ResponseEntity<?> getPaisYProvincia(Double latitud, Double longitud){
        return webApiCallerService.getEntityTokenOpcional(this.hechoServiceUrl + "/public/pais-provincia?latitud=" + latitud + "&longitud=" + longitud
                , PaisProvinciaDTO.class);
    }

    public ResponseEntity<?> getHechosDelUsuario(){
        // Llama al endpoint del backend: /api/hechos/mis-hechos
        return webApiCallerService.getList(
                this.hechoServiceUrl + "/mis-hechos",
                VisualizarHechosOutputDTO.class
        );
    }

    public ResponseEntity<List<VisualizarHechosOutputDTO>> getHechosDestacados() {
        return webApiCallerService.getListTokenOpcional(this.hechoServiceUrl + "/public/destacados", VisualizarHechosOutputDTO.class);
    }

    public ResponseEntity<?> eliminarHecho(Long id, String fuente){
        ResponseEntity<?> rta = webApiCallerService.postEntity(this.hechoServiceUrl + "/eliminar-hecho?id=" + id + "&fuente=" + fuente, RolCambiadoDTO.class);
        this.actualizarSesiones(rta);
        return rta;
    }

    public ResponseEntity<?> modificarHecho(HechoModificarInputDTO dto){
        return webApiCallerService.postEntity(this.hechoServiceUrl + "/modificar-hecho", dto, Void.class);
    }

    public ResponseEntity<Integer> getCantFuentes() {
        return webApiCallerService.getEntity(this.hechoServiceUrl + "/cantFuentes", Integer.class);
    }

    public ResponseEntity<?> getContenidoMultimediaHecho(Long id_hecho, String fuente){
        return webApiCallerService.getList(this.hechoServiceUrl + "/contenido-multimedia?id_hecho=" + id_hecho + "&fuente=" + fuente, ContenidoMultimedia.class);
    }

    private void actualizarSesiones(ResponseEntity<?> rta) {
        if (rta.getStatusCode().is2xxSuccessful()) {
            RolCambiadoDTO dtoOutput = (RolCambiadoDTO) rta.getBody();

            if (dtoOutput != null && dtoOutput.getRolModificado()) {
                List<HttpSession> sesionesActivas = this.activeSessionTracker
                        .sesionesAsociadasAUsuario(dtoOutput.getUsername());

                sesionesActivas.forEach(sesion -> {
                    SecurityContext context = (SecurityContext) sesion.getAttribute("SPRING_SECURITY_CONTEXT");

                    if (context != null) {
                        Authentication oldAuth = context.getAuthentication();

                        if (oldAuth != null) {
                            // Copiamos las authorities actuales en una lista mutable
                            List<GrantedAuthority> nuevasAuthorities = new ArrayList<>(oldAuth.getAuthorities());

                            // Eliminamos los roles anteriores (ROLE_)
                            nuevasAuthorities.removeIf(a -> a.getAuthority().startsWith("ROLE_"));

                            // Agregamos el nuevo rol
                            nuevasAuthorities.add(new SimpleGrantedAuthority("ROLE_" + dtoOutput.getRol().name()));

                            // Creamos una nueva Authentication con las nuevas authorities
                            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                                    oldAuth.getPrincipal(),
                                    oldAuth.getCredentials(),
                                    nuevasAuthorities
                            );

                            // Reemplazamos el Authentication en el SecurityContext
                            context.setAuthentication(newAuth);

                            // Persistimos el cambio en la sesión
                            sesion.setAttribute("SPRING_SECURITY_CONTEXT", context);
                        }
                    }
                });
            }
        }
    }
}
