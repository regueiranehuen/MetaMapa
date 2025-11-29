package modulos.Front.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import modulos.Front.BodyToListConverter;
import modulos.Front.dtos.input.CambiarContraseniaDtoInput;
import modulos.Front.dtos.input.EditarNombreDeUsuarioDtoInput;
import modulos.Front.dtos.input.EditarUsuarioDtoInput;
import modulos.Front.dtos.input.UsuarioInputDTO;
import modulos.Front.dtos.output.MensajeOutputDTO;
import modulos.Front.dtos.output.SolicitudHechoOutputDTO;
import modulos.Front.dtos.output.UsuarioOutputDto;
import modulos.Front.services.ColeccionService;
import modulos.Front.services.HechosService;
import modulos.Front.services.SolicitudHechoService;
import modulos.Front.services.UsuarioService;
import modulos.Front.usuario.Rol;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
@AllArgsConstructor
public class UsuarioController {

    private UsuarioService usuarioService;
    private final HechosService hechosService;
    private final ColeccionService coleccionService;
    private final SolicitudHechoService solicitudHechoService;


    @GetMapping("/cargar-register")
    public String cargarFormCrearUsuario(Model model){
        model.addAttribute("usuario", new UsuarioInputDTO());
        return "register";
    }

    @PostMapping("/registrar-usuario")
    public String crearUsuario(@Valid @ModelAttribute UsuarioInputDTO dtoInput, RedirectAttributes ra) {

        System.out.println("HOLA SOY UNA x JAJAJA Y RECIBI ESTO: " + dtoInput.getNombreUsuario());

        ResponseEntity<?> rta = this.usuarioService.crearUsuario(dtoInput);

        if(rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("msgExito", "Se registró correctamente");
            return "redirect:/login";
        }
        else if(rta.getBody() != null){
            ra.addAttribute("msgError", rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model) {

        ResponseEntity<?> rtaUsuario = usuarioService.getUsuario();
        if (!rtaUsuario.getStatusCode().is2xxSuccessful() || !rtaUsuario.hasBody()) {
            return "redirect:/404";
        }

        UsuarioOutputDto usuario = (UsuarioOutputDto) rtaUsuario.getBody();
        model.addAttribute("usuario", usuario);

        if (usuario.getRol().equals(Rol.ADMINISTRADOR)) {

            ResponseEntity<Long> statsHechos = hechosService.getCantHechos();
            ResponseEntity<Long> statsColecciones = coleccionService.getCantColecciones();
            ResponseEntity<Integer> statsSolicitudes = solicitudHechoService.getPorcentajeSolicitudesProcesadas();
            ResponseEntity<Integer> statsFuentes = hechosService.getCantFuentes();

            model.addAttribute("statsHechos", statsHechos.getBody());
            model.addAttribute("statsColecciones", statsColecciones.getBody());
            model.addAttribute("statsSolicitudes", statsSolicitudes.getBody());
            model.addAttribute("statsFuentes", statsFuentes.getBody());

            Long cantPendientes = 0L;
            ResponseEntity<?> rtaPendientes = solicitudHechoService.getSolicitudesPendientes();
            if (rtaPendientes.getStatusCode().is2xxSuccessful() && rtaPendientes.getBody() != null) {
                List<SolicitudHechoOutputDTO> solicitudes =
                        BodyToListConverter.bodyToList(rtaPendientes, SolicitudHechoOutputDTO.class);
                cantPendientes = (long) solicitudes.size();
            }

            model.addAttribute("solicitudesPendientes", cantPendientes);
        }

        ResponseEntity<?> mensajesDto = this.usuarioService.obtenerMensajes();


        if(mensajesDto.getStatusCode().is2xxSuccessful() && mensajesDto.getBody() != null) {
            List<MensajeOutputDTO> mensajes = BodyToListConverter.bodyToList(mensajesDto, MensajeOutputDTO.class);

            mensajes.forEach(mensaje -> System.out.println("MENSAJE: " + mensaje.getMensaje()));

            model.addAttribute("mensajes", mensajes);
        }

        return "perfil";
    }


    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONTRIBUYENTE', 'VISUALIZADOR')")
    @PostMapping("/editar/contrasenia")
    public String cambiarContrasenia(@Valid @ModelAttribute CambiarContraseniaDtoInput dto, RedirectAttributes ra, HttpServletRequest request){

        ResponseEntity<?> rta = this.usuarioService.cambiarContrasenia(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            return "redirect:/login?logout";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONTRIBUYENTE', 'VISUALIZADOR')")
    @PostMapping("/editar/campos-escalares")
    public String editarUsuario(@Valid @ModelAttribute EditarUsuarioDtoInput dto, RedirectAttributes ra){
        ResponseEntity<?> rta = this.usuarioService.editarUsuario(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            ra.addFlashAttribute("msgExito", "Campos editados correctamente");
            return "redirect:/usuarios/perfil";
        }
        else if(rta.getBody() != null){
            ra.addAttribute("msgError", rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CONTRIBUYENTE', 'VISUALIZADOR')")
    @PostMapping("/editar/nombre-usuario")
    public String editarNombreDeUsuario(@Valid @ModelAttribute EditarNombreDeUsuarioDtoInput dto, RedirectAttributes ra, HttpServletRequest request){
        ResponseEntity<?> rta = this.usuarioService.editarNombreDeUsuario(dto);

        if(rta.getStatusCode().is2xxSuccessful()){
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();
            return "redirect:/login?logout";
        }
        else if(rta.getBody() != null){
            ra.addAttribute("msgError", rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/get-all")
    public String getAll(Model model, RedirectAttributes ra){
        ResponseEntity<?> rta = usuarioService.getAll();

        if(rta.getStatusCode().is2xxSuccessful()){
            List<UsuarioOutputDto> usuarios = BodyToListConverter.bodyToList(rta, UsuarioOutputDto.class);

            model.addAttribute("usuarios", usuarios);
            return "usuarios";
        }
        else if(rta.getBody() != null){
            ra.addFlashAttribute(rta.getBody().toString());
        }
        return "redirect:/" + rta.getStatusCode().value();
    }
}
