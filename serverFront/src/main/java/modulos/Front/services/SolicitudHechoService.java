package modulos.Front.services;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import modulos.Front.dtos.input.*;
import modulos.Front.dtos.output.*;
import modulos.Front.sessionHandlers.ActiveSessionTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudHechoService {
    private final WebApiCallerService webApiCallerService;
    private final ActiveSessionTracker activeSessionTracker;
    private final HttpSession httpSession;
    private String solicitudHechoServiceUrl = "/api/solicitud-hecho";

    public SolicitudHechoService(WebApiCallerService webApiCallerService, ActiveSessionTracker activeSessionTracker, HttpSession httpSession) {
        this.webApiCallerService = webApiCallerService;
        this.activeSessionTracker = activeSessionTracker;
        this.httpSession = httpSession;
    }

    public ResponseEntity<?> evaluarSolicitudSubida(SolicitudHechoEvaluarInputDTO dtoInput) {
        ResponseEntity<?> rta = webApiCallerService.postEntity(solicitudHechoServiceUrl + "/evaluar/subir", dtoInput, RolCambiadoDTO.class);
        this.actualizarSesiones(rta);
        return rta;
    }

    public ResponseEntity<?> evaluarSolicitudEliminacion(SolicitudHechoEvaluarInputDTO dto) {
        ResponseEntity<?> rta = webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/evaluar/eliminar", dto, RolCambiadoDTO.class);
        this.actualizarSesiones(rta);
        return rta;
    }

    public ResponseEntity<?> getAllReportes(){
        return webApiCallerService.getList(this.solicitudHechoServiceUrl + "/reportes/get/all", SolicitudHechoOutputDTO.class);
    }

    public ResponseEntity<?> getAllSolicitudes(){
        return webApiCallerService.getList(this.solicitudHechoServiceUrl + "/get/all", SolicitudHechoOutputDTO.class);
    }

    public ResponseEntity<?> evaluarReporte(EvaluarReporteInputDTO dtoInput){
        ResponseEntity<?> rta = webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/reportes/evaluar", dtoInput, RolCambiadoDTO.class);
        this.actualizarSesiones(rta);
        return rta;
    }

    public ResponseEntity<?> getSolicitudesPendientes(){
        return webApiCallerService.getList(this.solicitudHechoServiceUrl + "/get/pendientes", SolicitudHechoOutputDTO.class);
    }

    public ResponseEntity<?> evaluarSolicitudModificacion(SolicitudHechoEvaluarInputDTO dto) {
        return webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/evaluar/modificar", dto, Void.class);
    }

    public ResponseEntity<?> enviarSolicitudEliminarHecho(SolicitudHechoEliminarInputDTO dto) {
        return webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/eliminar-hecho", dto, Void.class);
    }

    public ResponseEntity<?> enviarSolicitudModificarHecho(SolicitudHechoModificarInputDTO dto) {
        return webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/modificar-hecho", dto, Void.class);
    }

    public ResponseEntity<?> enviarSolicitudSubirHecho(SolicitudHechoInputDTO dto) {
        return webApiCallerService.postMultipartHechoTokenOpcional(this.solicitudHechoServiceUrl + "/public/subir-hecho", dto, Void.class);
    }

    public ResponseEntity<?> reportarHecho(String motivo, Long idHecho, String fuente) {
        return webApiCallerService.postEntity(this.solicitudHechoServiceUrl + "/reportes/reportar?id_hecho=" + idHecho + "&fuente=" + fuente + "&motivo=" + motivo, Void.class);
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

    public ResponseEntity<Integer> getPorcentajeSolicitudesProcesadas() {
        System.out.println("VOY A ENTRAR A SOLICITUD!!");
        return webApiCallerService.getEntityTokenOpcional(this.solicitudHechoServiceUrl + "/public/porcentajeSolicitudes", Integer.class);
    }

    public ResponseEntity<?> getAtributosHechoAModificar(Long id_solicitud){
        return webApiCallerService.getEntity(this.solicitudHechoServiceUrl + "/atributos-hecho?id_solicitud=" + id_solicitud, AtributosModificarDTO.class);
    }

}
